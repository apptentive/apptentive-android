/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.ApptentiveInternal;

import java.io.Serializable;

public class SessionData implements Serializable {

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

	public SessionData() {
		this.device = new Device();
		this.person = new Person();
	}

	//region Getters & Setters

	public String getConversationToken() {
		return conversationToken;
	}

	public void setConversationToken(String conversationToken) {
		this.conversationToken = conversationToken;
		save();
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
		save();
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
		save();
	}

	public String getPersonEmail() {
		return personEmail;
	}

	public void setPersonEmail(String personEmail) {
		this.personEmail = personEmail;
		save();
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
		save();
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
		save();
	}

	public Device getLastSentDevice() {
		return lastSentDevice;
	}

	public void setLastSentDevice(Device lastSentDevice) {
		this.lastSentDevice = lastSentDevice;
		save();
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
		save();
	}

	public Person getLastSentPerson() {
		return lastSentPerson;
	}

	public void setLastSentPerson(Person lastSentPerson) {
		this.lastSentPerson = lastSentPerson;
		save();
	}

	public Sdk getSdk() {
		return sdk;
	}

	public void setSdk(Sdk sdk) {
		this.sdk = sdk;
		save();
	}

	public AppRelease getAppRelease() {
		return appRelease;
	}

	public void setAppRelease(AppRelease appRelease) {
		this.appRelease = appRelease;
		save();
	}

	//endregion

	public void save() {
		ApptentiveInternal.getInstance().saveSessionData();
	}

	// version history
	// code point store
	// Various prefs
	// Messages
	// Interactions
}
