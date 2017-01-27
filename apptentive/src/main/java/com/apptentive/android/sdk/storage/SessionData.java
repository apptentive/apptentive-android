/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.text.TextUtils;

public class SessionData implements Saveable, DataChangedListener {

	private static final long serialVersionUID = 1L;

	private String conversationToken;
	private String conversationId;
	private String personId;
	private String personEmail;
	private String personName;
	private Device device;
	private Device lastSentDevice;
	private Person person;
	private Person lastSentPerson;
	private Sdk sdk;
	private AppRelease appRelease;
	private EventData eventData;
	private String lastSeenSdkVersion;
	private VersionHistory versionHistory;
	private boolean messageCenterFeatureUsed;
	private boolean messageCenterWhoCardPreviouslyDisplayed;
	private String messageCenterPendingMessage;
	private String messageCenterPendingAttachments;
	private String targets;
	private String interactions;
	private long interactionExpiration;

	public SessionData() {
		this.device = new Device();
		this.person = new Person();
		this.sdk = new Sdk();
		this.appRelease = new AppRelease();
		this.eventData = new EventData();
		this.versionHistory = new VersionHistory();
	}

	//region Listeners
	private transient DataChangedListener listener;

	@Override
	public void setDataChangedListener(DataChangedListener listener) {
		this.listener = listener;
		device.setDataChangedListener(this);
		person.setDataChangedListener(this);
		eventData.setDataChangedListener(this);
		versionHistory.setDataChangedListener(this);
	}

	@Override
	public void notifyDataChanged() {
		if (listener != null) {
			listener.onDataChanged();
		}
	}

	@Override
	public void onDataChanged() {
		notifyDataChanged();
	}
	//endregion

	//region Getters & Setters

	public String getConversationToken() {
		return conversationToken;
	}

	public void setConversationToken(String conversationToken) {
		if (!TextUtils.equals(this.conversationToken, conversationToken)) {
			this.conversationToken = conversationToken;
			notifyDataChanged();
		}
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		if (!TextUtils.equals(this.conversationId, conversationId)) {
			this.conversationId = conversationId;
			notifyDataChanged();
		}
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		if (!TextUtils.equals(this.personId, personId)) {
			this.personId = personId;
			notifyDataChanged();
		}
	}

	public String getPersonEmail() {
		return personEmail;
	}

	public void setPersonEmail(String personEmail) {
		if (!TextUtils.equals(this.personEmail, personEmail)) {
			this.personEmail = personEmail;
			notifyDataChanged();
		}
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		if (!TextUtils.equals(this.personName, personName)) {
			this.personName = personName;
			notifyDataChanged();
		}
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
		device.setDataChangedListener(this);
		notifyDataChanged();
	}

	public Device getLastSentDevice() {
		return lastSentDevice;
	}

	public void setLastSentDevice(Device lastSentDevice) {
		this.lastSentDevice = lastSentDevice;
		this.lastSentDevice.setDataChangedListener(this);
		notifyDataChanged();
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
		this.person.setDataChangedListener(this);
		notifyDataChanged();
	}

	public Person getLastSentPerson() {
		return lastSentPerson;
	}

	public void setLastSentPerson(Person lastSentPerson) {
		this.lastSentPerson = lastSentPerson;
		this.lastSentPerson.setDataChangedListener(this);
		notifyDataChanged();
	}

	public Sdk getSdk() {
		return sdk;
	}

	public void setSdk(Sdk sdk) {
		this.sdk = sdk;
		notifyDataChanged();
	}

	public AppRelease getAppRelease() {
		return appRelease;
	}

	public void setAppRelease(AppRelease appRelease) {
		this.appRelease = appRelease;
		notifyDataChanged();
	}

	public EventData getEventData() {
		return eventData;
	}

	public void setEventData(EventData eventData) {
		this.eventData = eventData;
		this.eventData.setDataChangedListener(this);
		notifyDataChanged();
	}

	public String getLastSeenSdkVersion() {
		return lastSeenSdkVersion;
	}

	public void setLastSeenSdkVersion(String lastSeenSdkVersion) {
		this.lastSeenSdkVersion = lastSeenSdkVersion;
		notifyDataChanged();
	}

	public VersionHistory getVersionHistory() {
		return versionHistory;
	}

	public void setVersionHistory(VersionHistory versionHistory) {
		this.versionHistory = versionHistory;
		this.versionHistory.setDataChangedListener(this);
		notifyDataChanged();
	}

	public boolean isMessageCenterFeatureUsed() {
		return messageCenterFeatureUsed;
	}

	public void setMessageCenterFeatureUsed(boolean messageCenterFeatureUsed) {
		if (this.messageCenterFeatureUsed != messageCenterFeatureUsed) {
			this.messageCenterFeatureUsed = messageCenterFeatureUsed;
			notifyDataChanged();
		}
	}

	public boolean isMessageCenterWhoCardPreviouslyDisplayed() {
		return messageCenterWhoCardPreviouslyDisplayed;
	}

	public void setMessageCenterWhoCardPreviouslyDisplayed(boolean messageCenterWhoCardPreviouslyDisplayed) {
		if (this.messageCenterWhoCardPreviouslyDisplayed != messageCenterWhoCardPreviouslyDisplayed) {
			this.messageCenterWhoCardPreviouslyDisplayed = messageCenterWhoCardPreviouslyDisplayed;
			notifyDataChanged();
		}
	}

	public String getMessageCenterPendingMessage() {
		return messageCenterPendingMessage;
	}

	public void setMessageCenterPendingMessage(String messageCenterPendingMessage) {
		if (!TextUtils.equals(this.messageCenterPendingMessage, messageCenterPendingMessage)) {
			this.messageCenterPendingMessage = messageCenterPendingMessage;
			notifyDataChanged();
		}
	}

	public String getMessageCenterPendingAttachments() {
		return messageCenterPendingAttachments;
	}

	public void setMessageCenterPendingAttachments(String messageCenterPendingAttachments) {
		if (!TextUtils.equals(this.messageCenterPendingAttachments, messageCenterPendingAttachments)) {
			this.messageCenterPendingAttachments = messageCenterPendingAttachments;
			notifyDataChanged();
		}
	}

	public String getTargets() {
		return targets;
	}

	public void setTargets(String targets) {
		if (!TextUtils.equals(this.targets, targets)) {
			this.targets = targets;
			notifyDataChanged();
		}
	}

	public String getInteractions() {
		return interactions;
	}

	public void setInteractions(String interactions) {
		if (!TextUtils.equals(this.interactions, interactions)) {
			this.interactions = interactions;
			notifyDataChanged();
		}
	}

	public long getInteractionExpiration() {
		return interactionExpiration;
	}

	public void setInteractionExpiration(long interactionExpiration) {
		if (this.interactionExpiration != interactionExpiration) {
			this.interactionExpiration = interactionExpiration;
			notifyDataChanged();
		}
	}

	//endregion
}
