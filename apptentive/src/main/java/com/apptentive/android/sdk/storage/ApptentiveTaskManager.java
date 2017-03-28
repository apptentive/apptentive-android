/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;

import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.model.Payload;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.notifications.ApptentiveNotification;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationObserver;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.apptentive.android.sdk.ApptentiveNotifications.*;
import static com.apptentive.android.sdk.conversation.ConversationState.*;
import static com.apptentive.android.sdk.debug.Assert.*;

public class ApptentiveTaskManager implements PayloadStore, EventStore, ApptentiveNotificationObserver {

	private ApptentiveDatabaseHelper dbHelper;
	private ThreadPoolExecutor singleThreadExecutor;

	// Set when receiving an ApptentiveNotification
	private String currentConversationId;

	/*
	 * Creates an asynchronous task manager with one worker thread. This constructor must be invoked on the UI thread.
	 */
	public ApptentiveTaskManager(Context context) {
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

		ApptentiveNotificationCenter.defaultCenter().addObserver(NOTIFICATION_CONVERSATION_STATE_DID_CHANGE, this);
	}


	/* Wrapper class that can be used to return worker thread result to caller through message
	*  Usage: Message message = callerThreadHandler.obtainMessage(MESSAGE_FINISH,
	*				     new AsyncTaskExResult<List<ApptentiveMessage>>(ApptentiveTaskManager.this, result));
	*			    message.sendToTarget();
	*/
	@SuppressWarnings({"RawUseOfParameterizedType"})
	private static class ApptentiveTaskResult<Data> {
		final ApptentiveTaskManager mTask;
		final Data[] mData;

		ApptentiveTaskResult(ApptentiveTaskManager task, Data... data) {
			mTask = task;
			mData = data;
		}
	}

	/**
	 * If an item with the same nonce as an item passed in already exists, it is overwritten by the item. Otherwise
	 * a new message is added.
	 */
	public void addPayload(final Payload... payloads) {
		for (Payload payload : payloads) {
			payload.setConversationId(currentConversationId);
		}
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				dbHelper.addPayload(payloads);
			}
		});
	}

	public void deletePayload(final Payload payload){
		if (payload != null) {
			singleThreadExecutor.execute(new Runnable() {
				@Override
				public void run() {
					dbHelper.deletePayload(payload);
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

	public synchronized Future<Payload> getOldestUnsentPayload() throws Exception {
		return singleThreadExecutor.submit(new Callable<Payload>() {
			@Override
			public Payload call() throws Exception {
				return dbHelper.getOldestUnsentPayload();
			}
		});
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

	public Future<Boolean> addCompoundMessageFiles(final List<StoredFile> associatedFiles) throws Exception{
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

	@Override
	public void onReceiveNotification(ApptentiveNotification notification) {
		if (notification.hasName(NOTIFICATION_CONVERSATION_STATE_DID_CHANGE)) {
			Conversation conversation = notification.getUserInfo(NOTIFICATION_KEY_CONVERSATION, Conversation.class);
			assertTrue(conversation != null && !conversation.hasState(UNDEFINED)); // sanity check
			if (conversation.hasActiveState()) {
				assertNotNull(conversation.getConversationId());
				currentConversationId = conversation.getConversationId();

				// when the Conversation ID comes back from the server, we need to update
				// the payloads that may have already been enqueued so
				// that they each have the Conversation ID.
				if (conversation.hasState(ANONYMOUS)) {
					singleThreadExecutor.execute(new Runnable() {
						@Override
						public void run() {
							dbHelper.updateMissingConversationIds(currentConversationId);
						}
					});
				}

			} else {
				currentConversationId = null;
			}
		}
	}
}