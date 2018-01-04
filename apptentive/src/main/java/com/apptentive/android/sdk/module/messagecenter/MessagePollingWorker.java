/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;


import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.util.Destroyable;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.apptentive.android.sdk.ApptentiveLogTag.*;

class MessagePollingWorker implements Destroyable {

	private final MessageManager messageManager;
	private final long backgroundPollingInterval;
	private final long foregroundPollingInterval;
	private final Configuration conf;

	/**
	 * Worker thread for background message fetching
	 */
	private MessagePollingThread pollingThread;

	final AtomicBoolean messageCenterInForeground = new AtomicBoolean(); // TODO: remove this flag

	MessagePollingWorker(MessageManager messageManager) {
		if (messageManager == null) {
			throw new IllegalArgumentException("Message manager is null");
		}

		this.messageManager = messageManager;

		conf = Configuration.load();
		backgroundPollingInterval = conf.getMessageCenterBgPoll() * 1000;
		foregroundPollingInterval = conf.getMessageCenterFgPoll() * 1000;
		ApptentiveLog.vv("Message Polling Worker: bg=%d, fg=%d", backgroundPollingInterval, foregroundPollingInterval);
	}

	@Override
	public void destroy() {
		stopPolling();
	}

	private class MessagePollingThread extends Thread {
		/**
		 * Flag indicating if message polling is active (message polling stops when this flag is set to
		 * <code>false</code>
		 */
		private final AtomicBoolean isPolling = new AtomicBoolean(true);

		/**
		 * Flag indicating if polling thread is busy with message fetching.
		 */
		private final AtomicBoolean isFetching = new AtomicBoolean(false);

		MessagePollingThread() {
			super("Message Polling Thread (" + getLocalConversationIdentifier() + ")");
		}

		@Override
		public void run() {
			try {
				ApptentiveLog.v(MESSAGES, "%s started", getName());

				while (isPolling.get()) {

					// sync poll message and mark thread as 'fetching'
					isFetching.set(true);
					pollMessagesSync();
					isFetching.set(false);

					// if we're done polling - no need to sleep
					if (!isPolling.get()) {
						break;
					}

					// sleep until next iteration
					long pollingInterval = messageCenterInForeground.get() ? foregroundPollingInterval : backgroundPollingInterval;
					ApptentiveLog.v(MESSAGES, "Scheduled polling messages in %d sec", pollingInterval / 1000);
					goToSleep(pollingInterval);
				}
			} catch (Exception e) {
				ApptentiveLog.e(e, "Exception while polling messages");
			} finally {
				ApptentiveLog.v(MESSAGES, "%s stopped", getName());
			}
		}

		private void pollMessagesSync() {
			try {
				if (ApptentiveInternal.getInstance().canShowMessageCenterInternal(getConversation())) {
					ApptentiveLog.v(MESSAGES, "Checking server for new messages...");
					messageManager.fetchAndStoreMessages(messageCenterInForeground.get(), conf.isMessageCenterNotificationPopupEnabled());
				} else {
					ApptentiveLog.w(MESSAGES, "Unable to fetch messages: message center can't be show at this time");
				}
			} catch (Exception e) {
				ApptentiveLog.e(MESSAGES, e, "Exception while polling messages");
			}
		}

		private void goToSleep(long millis) {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				ApptentiveLog.vv(MESSAGES, Thread.currentThread().getName() + " interrupted from sleep");
			}
		}

		/**
		 * Stops current polling
		 */
		void stopPolling() {
			isPolling.set(false);
			interrupt();
		}

		/**
		 * Wake thread from a sleep
		 */
		void wakeUp() {
			if (!isFetching.get()) {
				interrupt();
			} else {
				ApptentiveLog.vv("Can't wake up polling thread while it's synchronously fetching new messages");
			}
		}
	}

	// Called from main UI thread to create a new worker thread
	void appWentToForeground() {
		startPolling();
	}

	void appWentToBackground() {
		stopPolling();
	}

	/**
	 * If coming from the background, wake the thread so that it immediately starts runs and runs more often. If coming
	 * from the foreground, let the isPolling interval timeout naturally, at which point the isPolling interval will become
	 * the background isPolling interval.
	 *
	 * @param foreground true if the worker should be in foreground isPolling mode, else false.
	 */
	public void setMessageCenterInForeground(boolean foreground) {
		messageCenterInForeground.set(foreground);
		if (foreground) {
			startPolling();
		}
	}

	synchronized void startPolling() {
		ApptentiveLog.v(MESSAGES, "Start polling messages (%s)", getLocalConversationIdentifier());
		if (pollingThread == null) {
			pollingThread = new MessagePollingThread();
			pollingThread.start();
		} else {
			pollingThread.wakeUp();
		}
	}

	synchronized void stopPolling() {
		ApptentiveLog.v(MESSAGES, "Stop polling messages (%s)", getLocalConversationIdentifier());
		if (pollingThread != null) {
			pollingThread.stopPolling();
			pollingThread = null;
		}
	}

	private Conversation getConversation() {
		return messageManager.getConversation();
	}

	private String getLocalConversationIdentifier() {
		return getConversation().getLocalIdentifier();
	}
}
