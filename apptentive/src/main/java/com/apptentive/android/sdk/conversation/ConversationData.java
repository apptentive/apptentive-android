/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.debug.Assert;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.DataChangedListener;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.DeviceDataChangedListener;
import com.apptentive.android.sdk.storage.EventData;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.PersonDataChangedListener;
import com.apptentive.android.sdk.storage.Saveable;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.storage.VersionHistory;
import com.apptentive.android.sdk.util.StringUtils;

import java.util.UUID;

public class ConversationData implements Saveable, DataChangedListener, DeviceDataChangedListener, PersonDataChangedListener {

	private static final long serialVersionUID = 1L;
	private String localIdentifier;
	private String conversationToken;
	private String conversationId;
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
	private double interactionExpiration;

	public ConversationData() {
		this.localIdentifier = UUID.randomUUID().toString();
		this.device = new Device();
		this.person = new Person();
		this.sdk = new Sdk();
		this.appRelease = new AppRelease();
		this.eventData = new EventData();
		this.versionHistory = new VersionHistory();
	}

	//region Listeners

	private transient DataChangedListener listener;
	private transient PersonDataChangedListener personDataListener;
	private transient DeviceDataChangedListener deviceDataListener;

	public void setPersonDataListener(PersonDataChangedListener personDataListener) {
		this.personDataListener = personDataListener;
	}

	public void setDeviceDataListener(DeviceDataChangedListener deviceDataListener) {
		this.deviceDataListener = deviceDataListener;
	}

	@Override
	public void setDataChangedListener(DataChangedListener listener) {
		this.listener = listener;
		device.setDataChangedListener(this);
		device.setDeviceDataChangedListener(this);
		person.setDataChangedListener(this);
		person.setPersonDataChangedListener(this);
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

	@Override
	public void onDeviceDataChanged() {
		if (deviceDataListener != null) {
			deviceDataListener.onDeviceDataChanged();
		}
	}

	@Override
	public void onPersonDataChanged() {
		if (personDataListener != null) {
			personDataListener.onPersonDataChanged();
		}
	}

	//endregion

	//region Getters & Setters

	public String getLocalIdentifier() {
		return localIdentifier;
	}

	public String getConversationToken() {
		return conversationToken;
	}

	public void setConversationToken(String conversationToken) {
		if (!StringUtils.equal(this.conversationToken, conversationToken)) {
			this.conversationToken = conversationToken;
			notifyDataChanged();
		}
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		if (conversationId == null) {
			throw new IllegalArgumentException("Conversation id is null");
		}

		if (!StringUtils.equal(this.conversationId, conversationId)) {
			this.conversationId = conversationId;
			notifyDataChanged();
		}
	}

	public @NonNull Device getDevice() {
		return device;
	}

	public void setDevice(@NonNull Device device) {
		Assert.assertNotNull(device, "Device may not be null.");
		this.device = device;
		device.setDataChangedListener(this);
		device.setDeviceDataChangedListener(this);
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

	public @NonNull Person getPerson() {
		return person;
	}

	public void setPerson(@NonNull Person person) {
		Assert.assertNotNull(person, "Person may not be null.");
		this.person = person;
		this.person.setDataChangedListener(this);
		this.person.setPersonDataChangedListener(this);
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
		if (!StringUtils.equal(this.messageCenterPendingMessage, messageCenterPendingMessage)) {
			this.messageCenterPendingMessage = messageCenterPendingMessage;
			notifyDataChanged();
		}
	}

	public String getMessageCenterPendingAttachments() {
		return messageCenterPendingAttachments;
	}

	public void setMessageCenterPendingAttachments(String messageCenterPendingAttachments) {
		if (!StringUtils.equal(this.messageCenterPendingAttachments, messageCenterPendingAttachments)) {
			this.messageCenterPendingAttachments = messageCenterPendingAttachments;
			notifyDataChanged();
		}
	}

	public String getTargets() {
		return targets;
	}

	public void setTargets(String targets) {
		if (!StringUtils.equal(this.targets, targets)) {
			this.targets = targets;
			notifyDataChanged();
		}
	}

	public String getInteractions() {
		return interactions;
	}

	public void setInteractions(String interactions) {
		if (!StringUtils.equal(this.interactions, interactions)) {
			this.interactions = interactions;
			notifyDataChanged();
		}
	}

	public double getInteractionExpiration() {
		return interactionExpiration;
	}

	public void setInteractionExpiration(double interactionExpiration) {
		if (this.interactionExpiration != interactionExpiration) {
			this.interactionExpiration = interactionExpiration;
			notifyDataChanged();
		}
	}

	public @Nullable String getMParticleId() {
		return getPerson().getMParticleId();
	}

	public void setMParticleId(@Nullable String mParticleId) {
		getPerson().setMParticleId(mParticleId);
	}

	//endregion
}
