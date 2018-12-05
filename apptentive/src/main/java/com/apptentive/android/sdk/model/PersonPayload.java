/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONException;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class PersonPayload extends JsonPayload {

	public static final String KEY = "person";

	private static final String KEY_ID = "id";
	private static final String KEY_EMAIL = "email";
	private static final String KEY_NAME = "name";
	private static final String KEY_FACEBOOK_ID = "facebook_id";
	private static final String KEY_PHONE_NUMBER = "phone_number";
	private static final String KEY_STREET = "street";
	private static final String KEY_CITY = "city";
	private static final String KEY_ZIP = "zip";
	private static final String KEY_COUNTRY = "country";
	private static final String KEY_BIRTHDAY = "birthday";
	@SensitiveDataKey
	private static final String KEY_M_PARTICLE_ID = "mparticle_id";
	@SensitiveDataKey
	private static final String KEY_CUSTOM_DATA = "custom_data";

	static {
		registerSensitiveKeys(PersonPayload.class);
	}

	public PersonPayload() {
		super(PayloadType.person);
	}

	public PersonPayload(String json) throws JSONException {
		super(PayloadType.person, json);
	}

	//region Http-request


	@Override
	protected String getJsonContainer() {
		return KEY;
	}

	@Override
	public String getHttpEndPoint(String conversationId) {
		return StringUtils.format("/conversations/%s/person", conversationId);
	}

	//endregion

	public String getId() {
		return optString(KEY_ID, null);
	}

	public void setId(String id) {
		put(KEY_ID, id);
	}

	public String getEmail() {
		return optString(KEY_EMAIL, null);
	}

	public void setEmail(String email) {
		put(KEY_EMAIL, email);
	}

	public String getName() {
		return optString(KEY_NAME, null);
	}

	public void setName(String name) {
		put(KEY_NAME, name);
	}

	public void setFacebookId(String facebookId) {
		put(KEY_FACEBOOK_ID, facebookId);
	}

	public void setPhoneNumber(String phoneNumber) {
		put(KEY_PHONE_NUMBER, phoneNumber);
	}

	public void setStreet(String street) {
		put(KEY_STREET, street);
	}

	public void setCity(String city) {
		put(KEY_CITY, city);
	}

	public void setZip(String zip) {
		put(KEY_ZIP, zip);
	}

	public void setCountry(String country) {
		put(KEY_COUNTRY, country);
	}

	public void setBirthday(String birthday) {
		put(KEY_BIRTHDAY, birthday);
	}

	@SuppressWarnings("unchecked") // We check it coming in.
	public CustomData getCustomData() {
		if (!isNull(KEY_CUSTOM_DATA)) {
			try {
				return new CustomData(getJSONObject(KEY_CUSTOM_DATA));
			} catch (JSONException e) {
				logException(e);
			}
		}
		return null;
	}

	public void setCustomData(CustomData customData) {
		put(KEY_CUSTOM_DATA, customData);
	}

	public void setMParticleId(String mParticleId) {
		put(KEY_M_PARTICLE_ID, mParticleId);
	}
}
