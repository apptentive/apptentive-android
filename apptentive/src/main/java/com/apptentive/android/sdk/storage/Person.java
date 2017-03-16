/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.text.TextUtils;

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
	private CustomData customData;

	public Person() {
		customData = new CustomData();
	}

	//region Listeners
	private transient DataChangedListener listener;

	@Override
	public void setDataChangedListener(DataChangedListener listener) {
		this.listener = listener;
		customData.setDataChangedListener(this);
	}

	@Override
	public void notifyDataChanged() {
		if (listener != null) {
			listener.onDataChanged();
		}
	}

	@Override
	public void onDeserialize() {
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
		if (!TextUtils.equals(this.id, id)) {
			this.id = id;
			notifyDataChanged();
		}
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if (!TextUtils.equals(this.email, email)) {
			this.email = email;
			notifyDataChanged();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (!TextUtils.equals(this.name, name)) {
			this.name = name;
			notifyDataChanged();
		}
	}

	public String getFacebookId() {
		return facebookId;
	}

	public void setFacebookId(String facebookId) {
		if (!TextUtils.equals(this.facebookId, facebookId)) {
			this.facebookId = facebookId;
			notifyDataChanged();
		}
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		if (!TextUtils.equals(this.phoneNumber, phoneNumber)) {
			this.phoneNumber = phoneNumber;
			notifyDataChanged();
		}
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		if (!TextUtils.equals(this.street, street)) {
			this.street = street;
			notifyDataChanged();
		}
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		if (!TextUtils.equals(this.city, city)) {
			this.city = city;
			notifyDataChanged();
		}
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		if (!TextUtils.equals(this.zip, zip)) {
			this.zip = zip;
			notifyDataChanged();
		}
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		if (!TextUtils.equals(this.country, country)) {
			this.country = country;
			notifyDataChanged();
		}
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		if (!TextUtils.equals(this.birthday, birthday)) {
			this.birthday = birthday;
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
}
