/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;


import androidx.annotation.NonNull;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.model.ApptentiveMessage;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.util.Destroyable;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import java.util.List;

import static com.apptentive.android.sdk.ApptentiveHelper.dispatchOnConversationQueueOnce;
import static com.apptentive.android.sdk.ApptentiveLogTag.MESSAGES;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_INTERVAL;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_MESSAGES_STOPPED_POLLING;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_MESSAGES_STARTED_POLLING;
import static com.apptentive.android.sdk.debug.Assert.assertTrue;

class MessagePollingWorker implements Destroyable, MessageManager.MessageFetchListener {

	private final MessageManager messageManager;
	private final long backgroundPollingInterval;
	private final long foregroundPollingInterval;
	private final Configuration conf;
	private boolean messageCenterInForeground;

	private DispatchTask messagePollingTask;

	MessagePollingWorker(MessageManager messageManager) {
		if (messageManager == null) {
			throw new IllegalArgumentException("Message manager is null");
		}

		this.messageManager = messageManager;

		conf = Configuration.load();
		backgroundPollingInterval = conf.getMessageCenterBgPoll() * 1000;
		foregroundPollingInterval = conf.getMessageCenterFgPoll() * 1000;
		ApptentiveLog.v(MESSAGES, "Message Polling Worker: bg=%d, fg=%d", backgroundPollingInterval, foregroundPollingInterval);
	}

	@Override
	public void destroy() {
		stopPolling();
	}

	//region MessageFetchListener

	@Override
	public void onFetchFinish(MessageManager manager, List<ApptentiveMessage> messages) {
		if (isPolling()) {
			long pollingInterval = getPollingInterval();
			ApptentiveLog.v(MESSAGES, "Scheduled polling messages in %d sec", pollingInterval / 1000);
			dispatchOnConversationQueueOnce(messagePollingTask, pollingInterval);
		}
	}

	//endregion

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
	void setMessageCenterInForeground(boolean foreground) {
		messageCenterInForeground = foreground;
		if (foreground) {
			startPolling(true);
		}
	}

	void startPolling() {
		startPolling(false);
	}

	private void startPolling(boolean force) {
		if (force) {
			stopPolling();
		}

		if (!isPolling()) {
			ApptentiveLog.v(MESSAGES, "Start polling messages (%s)", getLocalConversationIdentifier());
			messagePollingTask = createPollingTask();
			dispatchOnConversationQueueOnce(messagePollingTask, 0L);
			notifyStartPolling(getPollingInterval());
		}
	}

	void stopPolling() {
		if (isPolling()) {
			ApptentiveLog.v(MESSAGES, "Stop polling messages (%s)", getLocalConversationIdentifier());
			messagePollingTask.cancel();
			messagePollingTask = null;
			notifyStopPolling();
		}
	}

	private long getPollingInterval() {
		return messageCenterInForeground ? foregroundPollingInterval : backgroundPollingInterval;
	}

	//region Notifications

	private void notifyStartPolling(long interval) {
		ApptentiveNotificationCenter.defaultCenter()
			.postNotification(NOTIFICATION_MESSAGES_STARTED_POLLING,
				NOTIFICATION_KEY_INTERVAL, interval);
	}

	private void notifyStopPolling() {
		ApptentiveNotificationCenter.defaultCenter().postNotification(NOTIFICATION_MESSAGES_STOPPED_POLLING);
	}

	//endregion

	private Conversation getConversation() {
		return messageManager.getConversation();
	}

	private String getLocalConversationIdentifier() {
		return getConversation().getLocalIdentifier();
	}

	boolean isMessageCenterInForeground() {
		return messageCenterInForeground;
	}

	private boolean isPolling() {
		return messagePollingTask != null;
	}

	@NonNull
	private DispatchTask createPollingTask() {
		return new DispatchTask() { // TODO: convert to ConversationDispatchTask
			@Override
			protected void execute() {
				assertTrue(isPolling(), "Not polling messages");

				if (ApptentiveInternal.canShowMessageCenterInternal(getConversation())) {
					ApptentiveLog.d(MESSAGES, "Checking server for new messages...");
					messageManager.fetchAndStoreMessages(messageCenterInForeground, conf.isMessageCenterNotificationPopupEnabled(), MessagePollingWorker.this);
				} else {
					ApptentiveLog.w(MESSAGES, "Unable to fetch messages: message center can't be show at this time");
				}
			}
		};
	}
}