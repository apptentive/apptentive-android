/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import androidx.annotation.Nullable;

import com.apptentive.android.sdk.util.StringUtils;

public class Person implements Saveable, DataChangedListener {

	private static final long serialVersionUID = 1L;

	private String id;
	private String email;
	private String name;
	private String facebookId;
	private String phoneNumber;
	private String street;
	private String city;
	private String zip;
	private String country;
	private String birthday;
	private String mParticleId;
	private CustomData customData;

	public Person() {
		customData = new CustomData();
	}

	//region Listeners
	private transient DataChangedListener listener;
	private transient PersonDataChangedListener personDataChangedListener;

	public void setPersonDataChangedListener(PersonDataChangedListener personDataChangedListener) {
		this.personDataChangedListener = personDataChangedListener;
	}

	@Override
	public void setDataChangedListener(DataChangedListener listener) {
		this.listener = listener;
		customData.setDataChangedListener(this);
	}

	@Override
	public void notifyDataChanged() {
		if (personDataChangedListener != null) {
			personDataChangedListener.onPersonDataChanged();
		}
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		if (!StringUtils.equal(this.id, id)) {
			this.id = id;
			notifyDataChanged();
		}
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if (!StringUtils.equal(this.email, email)) {
			this.email = email;
			notifyDataChanged();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (!StringUtils.equal(this.name, name)) {
			this.name = name;
			notifyDataChanged();
		}
	}

	public String getFacebookId() {
		return facebookId;
	}

	public void setFacebookId(String facebookId) {
		if (!StringUtils.equal(this.facebookId, facebookId)) {
			this.facebookId = facebookId;
			notifyDataChanged();
		}
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		if (!StringUtils.equal(this.phoneNumber, phoneNumber)) {
			this.phoneNumber = phoneNumber;
			notifyDataChanged();
		}
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		if (!StringUtils.equal(this.street, street)) {
			this.street = street;
			notifyDataChanged();
		}
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		if (!StringUtils.equal(this.city, city)) {
			this.city = city;
			notifyDataChanged();
		}
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		if (!StringUtils.equal(this.zip, zip)) {
			this.zip = zip;
			notifyDataChanged();
		}
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		if (!StringUtils.equal(this.country, country)) {
			this.country = country;
			notifyDataChanged();
		}
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		if (!StringUtils.equal(this.birthday, birthday)) {
			this.birthday = birthday;
			notifyDataChanged();
		}
	}

	public @Nullable String getMParticleId() {
		return mParticleId;
	}

	public void setMParticleId(String mParticleId) {
		if (!StringUtils.equal(this.mParticleId, mParticleId)) {
			this.mParticleId = mParticleId;
			notifyDataChanged();
		}
	}

	public CustomData getCustomData() {
		return customData;
	}

	public void setCustomData(CustomData customData) {
		this.customData = customData;
		this.customData.setDataChangedListener(this);
		notifyDataChanged();
	}

	//endregion
	
	//region Clone

	public Person clone() {
		Person person = new Person();
		person.id = id;
		person.email = email;
		person.name = name;
		person.facebookId = facebookId;
		person.phoneNumber = phoneNumber;
		person.street = street;
		person.city = city;
		person.zip = zip;
		person.country = country;
		person.birthday = birthday;
		if (customData != null) {
			person.customData.putAll(customData);
		}
		person.listener = listener;
		return person;
	}

	//endregion
}
