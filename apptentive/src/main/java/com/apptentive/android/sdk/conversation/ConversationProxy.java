/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.ApptentiveNotifications;
import com.apptentive.android.sdk.model.SurveyResponsePayload;
import com.apptentive.android.sdk.notifications.ApptentiveNotification;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationObserver;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.EventData;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.VersionHistory;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import static com.apptentive.android.sdk.ApptentiveHelper.dispatchOnConversationQueue;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_CONVERSATION_DATA_DID_CHANGE;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_CONVERSATION_STATE_DID_CHANGE;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_MESSAGE_STORE_DID_CHANGE;

public class ConversationProxy implements ApptentiveNotificationObserver {
	private final Conversation conversation;
	private String personName;
	private String personEmail;
	private String messageCenterPendingMessage;
	private String messageCenterPendingAttachments;
	private int unreadMessageCount;
	private boolean hasActiveState;
	private boolean messageCenterWhoCardPreviouslyDisplayed;

	public ConversationProxy(Conversation conversation) {
		if (conversation == null) {
			throw new IllegalArgumentException("Conversation is null");
		}
		this.conversation = conversation;
		synchronize();
		registerNotifications();
	}

	//region Notifications

	private void registerNotifications() {
		ApptentiveNotificationCenter.defaultCenter()
				.addObserver(NOTIFICATION_CONVERSATION_DATA_DID_CHANGE, this)
				.addObserver(NOTIFICATION_CONVERSATION_STATE_DID_CHANGE, this)
				.addObserver(NOTIFICATION_MESSAGE_STORE_DID_CHANGE, this);
	}

	@Override
	public synchronized void onReceiveNotification(ApptentiveNotification notification) {
		synchronize();
	}

	//endregion

	//region Synchronization

	private synchronized void synchronize() {
		personEmail = conversation.getPerson().getEmail();
		personName = conversation.getPerson().getName();
		messageCenterPendingMessage = conversation.getMessageCenterPendingMessage();
		messageCenterPendingAttachments = conversation.getMessageCenterPendingAttachments();
		hasActiveState = conversation.hasActiveState();
		messageCenterWhoCardPreviouslyDisplayed = conversation.isMessageCenterWhoCardPreviouslyDisplayed();
		unreadMessageCount = conversation.getMessageManager().getUnreadMessageCount();
	}

	//endregion

	//region Payload

	public void addPayload(final SurveyResponsePayload payload) {
		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				conversation.addPayload(payload);
			}
		});
	}

	//endregion

	//region Getters/Setters

	public synchronized String getPersonEmail() {
		return personEmail;
	}

	public synchronized void setPersonEmail(final String personEmail) {
		this.personEmail = personEmail;
		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				conversation.getPerson().setEmail(personEmail);
			}
		});
	}

	public synchronized String getPersonName() {
		return personName;
	}

	public synchronized void setPersonName(final String personName) {
		this.personName = personName;
		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				conversation.getPerson().setName(personName);
			}
		});
	}

	public synchronized void setMessageCenterPendingMessage(final String messageCenterPendingMessage) {
		this.messageCenterPendingMessage = messageCenterPendingMessage;
		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				conversation.setMessageCenterPendingMessage(messageCenterPendingMessage);
			}
		});
	}

	public synchronized String getMessageCenterPendingMessage() {
		return messageCenterPendingMessage;
	}

	public synchronized void setMessageCenterPendingAttachments(final String messageCenterPendingAttachments) {
		this.messageCenterPendingAttachments = messageCenterPendingAttachments;
		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				conversation.setMessageCenterPendingAttachments(messageCenterPendingAttachments);
			}
		});
	}

	public synchronized String getMessageCenterPendingAttachments() {
		return messageCenterPendingAttachments;
	}

	public synchronized void setMessageCenterWhoCardPreviouslyDisplayed(final boolean messageCenterWhoCardPreviouslyDisplayed) {
		this.messageCenterWhoCardPreviouslyDisplayed = messageCenterWhoCardPreviouslyDisplayed;
		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				conversation.setMessageCenterWhoCardPreviouslyDisplayed(messageCenterWhoCardPreviouslyDisplayed);
			}
		});
	}

	public synchronized void setMessageCenterInForeground(final boolean flag) {
		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				conversation.getMessageManager().setMessageCenterInForeground(flag);
			}
		});
	}

	public synchronized boolean isMessageCenterWhoCardPreviouslyDisplayed() {
		return messageCenterWhoCardPreviouslyDisplayed;
	}

	public synchronized boolean hasActiveState() {
		return hasActiveState;
	}

	public synchronized int getUnreadMessageCount() {
		return unreadMessageCount;
	}

	public synchronized Person getPerson() {
		return conversation.getPerson(); // TODO: make conversation immutable
	}

	public synchronized Device getDevice() {
		return conversation.getDevice(); // TODO: make conversation immutable
	}

	public synchronized AppRelease getAppRelease() {
		return conversation.getAppRelease(); // TODO: make conversation immutable
	}

	public synchronized EventData getEventData() {
		return conversation.getEventData(); // TODO: make conversation immutable
	}

	public synchronized String getInteractions() {
		return conversation.getInteractions(); // TODO: make conversation immutable
	}

	public synchronized VersionHistory getVersionHistory() {
		return conversation.getVersionHistory();
	}

	public String getConversationToken() {
		return conversation.getConversationToken();
	}

	//endregion
}
