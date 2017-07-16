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

import static com.apptentive.android.sdk.ApptentiveLogTag.MESSAGES;

class MessagePollingWorker implements Destroyable {

	private MessagePollingThread pollingThread;

	private final MessageManager manager;
	final AtomicBoolean messageCenterInForeground = new AtomicBoolean();

	MessagePollingWorker(MessageManager manager) {
		this.manager = manager;
	}

	@Override
	public void destroy() {
		stopPolling();
	}

	private class MessagePollingThread extends Thread {

		private final long backgroundPollingInterval;
		private final long foregroundPollingInterval;
		private final Configuration conf;

		public MessagePollingThread() {
			super("Message Polling Thread");
			conf = Configuration.load();
			backgroundPollingInterval = conf.getMessageCenterBgPoll() * 1000;
			foregroundPollingInterval = conf.getMessageCenterFgPoll() * 1000;
		}

		public void run() {
			try {
				ApptentiveLog.v(MESSAGES, "Started polling messages");

				while (!Thread.currentThread().isInterrupted()) {
					long pollingInterval = messageCenterInForeground.get() ? foregroundPollingInterval : backgroundPollingInterval;
					if (ApptentiveInternal.getInstance().canShowMessageCenterInternal(getConversation())) {
						ApptentiveLog.v(MESSAGES, "Checking server for new messages...");
						manager.fetchAndStoreMessages(messageCenterInForeground.get(), conf.isMessageCenterNotificationPopupEnabled());
					}

					ApptentiveLog.v(MESSAGES, "Polling messages in %d sec", pollingInterval / 1000);
					if (Thread.currentThread().isInterrupted() || !goToSleep(pollingInterval)) {
						break;
					}
				}
			} finally {
				ApptentiveLog.v(MESSAGES, "Stopped polling messages");
			}
		}
	}

	private boolean goToSleep(long millis) {
		try {
			Thread.sleep(millis);
			return true;
		} catch (InterruptedException e) {
			return false;
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
	 * from the foreground, let the polling interval timeout naturally, at which point the polling interval will become
	 * the background polling interval.
	 *
	 * @param foreground true if the worker should be in foreground polling mode, else false.
	 */
	public void setMessageCenterInForeground(boolean foreground) {
		messageCenterInForeground.set(foreground);
		if (foreground) {
			startPolling();
		}
	}

	synchronized void startPolling() {
		stopPolling();

		pollingThread = new MessagePollingThread();
		pollingThread.start();
	}

	synchronized void stopPolling() {
		if (pollingThread != null) {
			pollingThread.interrupt();
			pollingThread = null;
		}
	}

	private Conversation getConversation() {
		return manager.getConversation();
	}
}
