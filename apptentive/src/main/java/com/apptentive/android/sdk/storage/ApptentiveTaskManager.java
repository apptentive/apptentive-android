/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.comm.ApptentiveHttpClient;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.conversation.ConversationState;
import com.apptentive.android.sdk.encryption.EncryptionKey;
import com.apptentive.android.sdk.model.Payload;
import com.apptentive.android.sdk.model.PayloadData;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.network.HttpRequestRetryPolicyDefault;
import com.apptentive.android.sdk.notifications.ApptentiveNotification;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationObserver;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.apptentive.android.sdk.ApptentiveHelper.checkConversationQueue;
import static com.apptentive.android.sdk.ApptentiveHelper.conversationQueue;
import static com.apptentive.android.sdk.ApptentiveHelper.dispatchOnConversationQueue;
import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;
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
import static com.apptentive.android.sdk.debug.Assert.notNull;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class ApptentiveTaskManager implements PayloadStore, EventStore, ApptentiveNotificationObserver, PayloadSender.Listener {

	private final ApptentiveDatabaseHelper dbHelper;
	private final ThreadPoolExecutor singleThreadExecutor; // TODO: replace with a private concurrent dispatch queue

	private final PayloadSender payloadSender;
	private boolean appInBackground = true;

	/*
	 * Creates an asynchronous task manager with one worker thread. This constructor must be invoked on the UI thread.
	 */
	public ApptentiveTaskManager(Context context, ApptentiveHttpClient apptentiveHttpClient, Encryption encryption) {
		dbHelper = new ApptentiveDatabaseHelper(context, encryption);
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
				return false; // don't use built-in retry logic for payloads since payload sender listener
											// would handle it properly
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
	public void addPayload(final Payload payload) {
		ApptentiveLog.v(PAYLOADS, "Adding payload: %s", payload);
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					dbHelper.addPayload(payload);
					sendNextPayloadSync();
				} catch (Exception e) {
					ApptentiveLog.e(PAYLOADS, e, "Exception while adding a payload: %s", payload);
					logException(e);
				}
			}
		});
	}

	public void deletePayload(final String payloadIdentifier) {
		if (payloadIdentifier != null) {
			singleThreadExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						dbHelper.deletePayload(payloadIdentifier);
						sendNextPayloadSync();
					} catch (Exception e) {
						ApptentiveLog.e(PAYLOADS, e, "Exception while deleting a payload: %s", payloadIdentifier);
						logException(e);
					}
				}
			});
		}
	}

	public void deleteAllPayloads() {
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					dbHelper.deleteAllPayloads();
				} catch (Exception e) {
					ApptentiveLog.e(PAYLOADS, e, "Exception while deleting all payloads");
					logException(e);
				}
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
				try {
					dbHelper.deleteAssociatedFiles(messageNonce);
				} catch (Exception e) {
					ApptentiveLog.e(PAYLOADS, e, "Exception while deleting associated file: %s", messageNonce);
					logException(e);
				}
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
			ApptentiveLog.e(PAYLOADS, "Payload sending failed: %s\n%s", payload, errorMessage);
			if (appInBackground) {
				ApptentiveLog.v(PAYLOADS, "The app went to the background so we won't remove the payload from the queue");
				retrySending(5000);
				return;
			} else if (responseCode == -1) {
				ApptentiveLog.v(PAYLOADS, "Payload failed to send due to a connection error.");
				retrySending(5000);
				return;
			} else if (responseCode >= 500) {
				ApptentiveLog.v(PAYLOADS, "Payload failed to send due to a server error.");
				retrySending(5000);
				return;
			}
		} else {
			ApptentiveLog.v(PAYLOADS, "Payload was successfully sent: %s", payload);
		}

		// Only let the payload be deleted if it was successfully sent, or got an unrecoverable client error.
		deletePayload(payload.getNonce());
	}

	private void retrySending(long delayMillis) {
		ApptentiveLog.d(PAYLOADS, "Retry sending payloads in %d ms", delayMillis);
		conversationQueue().dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				singleThreadExecutor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							ApptentiveLog.d(PAYLOADS, "Retrying sending payloads");
							sendNextPayloadSync();
						} catch (Exception e) {
							ApptentiveLog.e(PAYLOADS, e, "Exception while trying to retry sending payloads");
							logException(e);
						}
					}
				});
			}
		}, delayMillis);
	}

	//endregion

	//region Payload Sending
	private void sendNextPayload() {
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					sendNextPayloadSync();
				} catch (Exception e) {
					ApptentiveLog.e(e, "Exception while trying to send next payload");
					logException(e);
				}
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
			logException(e);
			return;
		}

		if (payload == null) {
			return;
		}

		boolean scheduled = payloadSender.sendPayload(payload);

		// if payload sending was scheduled - notify the rest of the SDK
		if (scheduled) {
			dispatchOnConversationQueue(new DispatchTask() {
				@Override
				protected void execute() {
					ApptentiveNotificationCenter.defaultCenter()
							.postNotification(NOTIFICATION_PAYLOAD_WILL_START_SEND, NOTIFICATION_KEY_PAYLOAD, payload);
				}
			});
		}
	}

	//endregion

	@Override
	public void onReceiveNotification(ApptentiveNotification notification) {
		checkConversationQueue();

		if (notification.hasName(NOTIFICATION_CONVERSATION_STATE_DID_CHANGE)) {
			final Conversation conversation = notification.getUserInfo(NOTIFICATION_KEY_CONVERSATION, Conversation.class);
			assertNotNull(conversation); // sanity check
			assertNotEquals(conversation.getState(), UNDEFINED);
			if (conversation.hasActiveState()) {
				final String conversationId = notNull(conversation.getConversationId());
				final String conversationToken = notNull(conversation.getConversationToken());
				final String conversationLocalIdentifier = notNull(conversation.getLocalIdentifier());
				final boolean legacyPayloads = ConversationState.LEGACY_PENDING.equals(conversation.getPrevState());

				ApptentiveLog.d(CONVERSATION, "Conversation %s state changed %s -> %s.", conversationId, conversation.getPrevState(), conversation.getState());
				// when the Conversation ID comes back from the server, we need to update
				// the payloads that may have already been enqueued so
				// that they each have the Conversation ID.
				if (conversation.hasState(ANONYMOUS)) {
					singleThreadExecutor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								dbHelper.updateIncompletePayloads(conversationId, conversationToken, conversationLocalIdentifier, legacyPayloads);
								sendNextPayloadSync(); // after we've updated payloads - we need to send them
							} catch (Exception e) {
								ApptentiveLog.e(CONVERSATION, e, "Exception while trying to update incomplete payloads");
								logException(e);
							}
						}
					});
				}
			}
		} else if (notification.hasName(NOTIFICATION_APP_ENTERED_FOREGROUND)) {
			appInBackground = false;
			sendNextPayload(); // when the app comes back from the background - we need to resume sending payloads
		} else if (notification.hasName(NOTIFICATION_APP_ENTERED_BACKGROUND)) {
			appInBackground = true;
		}
	}
}
