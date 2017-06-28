/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.comm.ApptentiveHttpClient;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.debug.Assert;
import com.apptentive.android.sdk.model.Payload;
import com.apptentive.android.sdk.model.PayloadData;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.network.HttpRequestRetryPolicyDefault;
import com.apptentive.android.sdk.notifications.ApptentiveNotification;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationObserver;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.apptentive.android.sdk.ApptentiveLogTag.PAYLOADS;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_APP_ENTERED_BACKGROUND;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_APP_ENTERED_FOREGROUND;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_CONVERSATION_STATE_DID_CHANGE;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_CONVERSATION;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_PAYLOAD;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_RESPONSE_CODE;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_RESPONSE_DATA;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_SUCCESSFUL;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_PAYLOAD_DID_FINISH_SEND;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_PAYLOAD_WILL_START_SEND;
import static com.apptentive.android.sdk.conversation.ConversationState.ANONYMOUS;
import static com.apptentive.android.sdk.conversation.ConversationState.UNDEFINED;
import static com.apptentive.android.sdk.debug.Assert.assertNotEquals;
import static com.apptentive.android.sdk.debug.Assert.assertNotNull;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class ApptentiveTaskManager implements PayloadStore, EventStore, ApptentiveNotificationObserver, PayloadSender.Listener {

	private final ApptentiveDatabaseHelper dbHelper;
	private final ThreadPoolExecutor singleThreadExecutor; // TODO: replace with a private concurrent dispatch queue

	// Set when receiving an ApptentiveNotification
	private String currentConversationId;
	private String currentConversationToken;
	private String conversationEncryptionKey;

	private final PayloadSender payloadSender;
	private boolean appInBackground;

	/*
	 * Creates an asynchronous task manager with one worker thread. This constructor must be invoked on the UI thread.
	 */
	public ApptentiveTaskManager(Context context, ApptentiveHttpClient apptentiveHttpClient) {
		dbHelper = new ApptentiveDatabaseHelper(context);
		/* When a new database task is submitted, the executor has the following behaviors:
		 * 1. If the thread pool has no thread yet, it creates a single worker thread.
		 * 2. If the single worker thread is running with tasks, it queues tasks.
		 * 3. If the queue is full, the task will be rejected and run on caller thread.
		 *
		 */
		singleThreadExecutor = new ThreadPoolExecutor(1, 1,
			30L, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(),
			new ThreadPoolExecutor.CallerRunsPolicy());

		// If no new task arrives in 30 seconds, the worker thread terminates; otherwise it will be reused
		singleThreadExecutor.allowCoreThreadTimeOut(true);

		// Create payload sender object with a custom 'retry' policy
		payloadSender = new PayloadSender(apptentiveHttpClient, new HttpRequestRetryPolicyDefault() {
			@Override
			public boolean shouldRetryRequest(int responseCode, int retryAttempt) {
				if (appInBackground) {
					return false; // don't retry if the app went background
				}
				return super.shouldRetryRequest(responseCode, retryAttempt);
			}
		});
		payloadSender.setListener(this);

		ApptentiveNotificationCenter.defaultCenter()
			.addObserver(NOTIFICATION_CONVERSATION_STATE_DID_CHANGE, this)
			.addObserver(NOTIFICATION_APP_ENTERED_BACKGROUND, this)
			.addObserver(NOTIFICATION_APP_ENTERED_FOREGROUND, this);
	}

	/**
	 * If an item with the same nonce as an item passed in already exists, it is overwritten by the item. Otherwise
	 * a new message is added.
	 */
	public void addPayload(final Payload... payloads) {

		// Provide each payload with the information it will need to send itself to the server securely.
		for (Payload payload : payloads) {
			if (currentConversationId != null) {
				payload.setConversationId(currentConversationId);
			}
			if (currentConversationToken != null) {
				payload.setToken(currentConversationToken);
			}
			if (conversationEncryptionKey != null) {
				payload.setEncryptionKey(conversationEncryptionKey);
			}
		}
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				dbHelper.addPayload(payloads);
				sendNextPayloadSync();
			}
		});
	}

	public void deletePayload(final String payloadIdentifier) {
		if (payloadIdentifier != null) {
			singleThreadExecutor.execute(new Runnable() {
				@Override
				public void run() {
					dbHelper.deletePayload(payloadIdentifier);
					sendNextPayloadSync();
				}
			});
		}
	}

	public void deleteAllPayloads() {
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				dbHelper.deleteAllPayloads();
			}
		});
	}

	private PayloadData getOldestUnsentPayloadSync() {
		return dbHelper.getOldestUnsentPayload();
	}

	public void deleteAssociatedFiles(final String messageNonce) {
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				dbHelper.deleteAssociatedFiles(messageNonce);
			}
		});
	}

	public Future<List<StoredFile>> getAssociatedFiles(final String nonce) throws Exception {
		return singleThreadExecutor.submit(new Callable<List<StoredFile>>() {
			@Override
			public List<StoredFile> call() throws Exception {
				return dbHelper.getAssociatedFiles(nonce);
			}
		});
	}

	public Future<Boolean> addCompoundMessageFiles(final List<StoredFile> associatedFiles) throws Exception {
		return singleThreadExecutor.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return dbHelper.addCompoundMessageFiles(associatedFiles);
			}
		});
	}

	public void reset(Context context) {
		dbHelper.reset(context);
	}

	//region PayloadSender.Listener

	@Override
	public void onFinishSending(PayloadSender sender, PayloadData payload, boolean cancelled, String errorMessage, int responseCode, JSONObject responseData) {
		ApptentiveNotificationCenter.defaultCenter()
			.postNotification(NOTIFICATION_PAYLOAD_DID_FINISH_SEND,
				NOTIFICATION_KEY_PAYLOAD, payload,
				NOTIFICATION_KEY_SUCCESSFUL, errorMessage == null && !cancelled ? TRUE : FALSE,
				NOTIFICATION_KEY_RESPONSE_CODE, responseCode,
				NOTIFICATION_KEY_RESPONSE_DATA, responseData);

		if (cancelled) {
			ApptentiveLog.v(PAYLOADS, "Payload sending was cancelled: %s", payload);
			return; // don't remove cancelled payloads from the queue
		}

		if (errorMessage != null) {
			ApptentiveLog.v(PAYLOADS, "Payload sending failed: %s\n%s", payload, errorMessage);
			if (appInBackground) {
				ApptentiveLog.v(PAYLOADS, "The app went to the background so we won't remove the payload from the queue");
				return;
			} else if (responseCode == -1) {
				ApptentiveLog.v(PAYLOADS, "Payload failed to send due to a connection error.");
				return;
			} else if (responseCode > 500) {
				ApptentiveLog.v(PAYLOADS, "Payload failed to send due to a server error.");
				return;
			}
		} else {
			ApptentiveLog.v(PAYLOADS, "Payload was successfully sent: %s", payload);
		}

		// Only let the payload be deleted if it was successfully sent, or got an unrecoverable client error.
		deletePayload(payload.getNonce());
	}

	//endregion

	//region Payload Sending
	private void sendNextPayload() {
		DispatchQueue.backgroundQueue().dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				sendNextPayloadSync();
			}
		});
	}

	private void sendNextPayloadSync() {
		if (appInBackground) {
			ApptentiveLog.v(PAYLOADS, "Can't send the next payload: the app is in the background");
			return;
		}

		if (payloadSender.isSendingPayload()) {
			ApptentiveLog.v(PAYLOADS, "Can't send the next payload: payload sender is busy");
			return;
		}

		final PayloadData payload;
		try {
			payload = getOldestUnsentPayloadSync();
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while peeking the next payload for sending");
			return;
		}

		if (payload == null) {
			ApptentiveLog.v(PAYLOADS, "Can't send the next payload: no unsent payloads found");
			return;
		}

		boolean scheduled = payloadSender.sendPayload(payload);

		// if payload sending was scheduled - notify the rest of the SDK
		if (scheduled) {
			ApptentiveNotificationCenter.defaultCenter()
				.postNotification(NOTIFICATION_PAYLOAD_WILL_START_SEND, NOTIFICATION_KEY_PAYLOAD, payload);
		}
	}

	//endregion

	@Override
	public void onReceiveNotification(ApptentiveNotification notification) {
		if (notification.hasName(NOTIFICATION_CONVERSATION_STATE_DID_CHANGE)) {
			Conversation conversation = notification.getUserInfo(NOTIFICATION_KEY_CONVERSATION, Conversation.class);
			assertNotNull(conversation); // sanity check
			assertNotEquals(conversation.getState(), UNDEFINED);
			if (conversation.hasActiveState()) {
				assertNotNull(conversation.getConversationId());
				currentConversationId = conversation.getConversationId();
				Assert.assertNotNull(currentConversationId);

				currentConversationToken = conversation.getConversationToken();
				conversationEncryptionKey = conversation.getEncryptionKey();
				Assert.assertNotNull(currentConversationToken);
				ApptentiveLog.d("Conversation %s state changed to %s.", currentConversationId, conversation.getState());
				// when the Conversation ID comes back from the server, we need to update
				// the payloads that may have already been enqueued so
				// that they each have the Conversation ID.
				if (conversation.hasState(ANONYMOUS)) {
					singleThreadExecutor.execute(new Runnable() {
						@Override
						public void run() {
							dbHelper.updateIncompletePayloads(currentConversationId, currentConversationToken);
							sendNextPayloadSync(); // after we've updated payloads - we need to send them
						}
					});
				}

			} else {
				currentConversationId = null;
			}
		} else if (notification.hasName(NOTIFICATION_APP_ENTERED_FOREGROUND)) {
			appInBackground = false;
			sendNextPayload(); // when the app comes back from the background - we need to resume sending payloads
		} else if (notification.hasName(NOTIFICATION_APP_ENTERED_BACKGROUND)) {
			appInBackground = true;
		}
	}
}