/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.migration.v4_0_0;

import org.json.JSONException;
import org.json.JSONObject;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class Person extends JSONObject {

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
	private static final String KEY_CUSTOM_DATA = "custom_data";

	public Person(String json) throws JSONException {
		super(json);
	}

	public String getId() {
		try {
			if (!isNull(KEY_ID)) {
				return getString(KEY_ID);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setId(String id) {
		try {
			put(KEY_ID, id);
		} catch (JSONException e) {
			logException(e);
		}
	}

	public String getEmail() {
		try {
			if (!isNull(KEY_EMAIL)) {
				return getString(KEY_EMAIL);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setEmail(String email) {
		try {
			put(KEY_EMAIL, email);
		} catch (JSONException e) {
			logException(e);
		}
	}

	public String getName() {
		try {
			if (!isNull(KEY_NAME)) {
				return getString(KEY_NAME);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setName(String name) {
		try {
			put(KEY_NAME, name);
		} catch (JSONException e) {
			logException(e);
		}
	}

	public String getFacebookId() {
		try {
			if (!isNull(KEY_FACEBOOK_ID)) {
				return getString(KEY_FACEBOOK_ID);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setFacebookId(String facebookId) {
		try {
			put(KEY_FACEBOOK_ID, facebookId);
		} catch (JSONException e) {
			logException(e);
		}
	}

	public String getPhoneNumber() {
		try {
			if (!isNull(KEY_PHONE_NUMBER)) {
				return getString(KEY_PHONE_NUMBER);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setPhoneNumber(String phoneNumber) {
		try {
			put(KEY_PHONE_NUMBER, phoneNumber);
		} catch (JSONException e) {
			logException(e);
		}
	}

	public String getStreet() {
		try {
			if (!isNull(KEY_STREET)) {
				return getString(KEY_STREET);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setStreet(String street) {
		try {
			put(KEY_STREET, street);
		} catch (JSONException e) {
			logException(e);
		}
	}

	public String getCity() {
		try {
			if (!isNull(KEY_CITY)) {
				return getString(KEY_CITY);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setCity(String city) {
		try {
			put(KEY_CITY, city);
		} catch (JSONException e) {
			logException(e);
		}
	}

	public String getZip() {
		try {
			if (!isNull(KEY_ZIP)) {
				return getString(KEY_ZIP);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setZip(String zip) {
		try {
			put(KEY_ZIP, zip);
		} catch (JSONException e) {
			logException(e);
		}
	}

	public String getCountry() {
		try {
			if (!isNull(KEY_COUNTRY)) {
				return getString(KEY_COUNTRY);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setCountry(String country) {
		try {
			put(KEY_COUNTRY, country);
		} catch (JSONException e) {
			logException(e);
		}
	}

	public String getBirthday() {
		try {
			if (!isNull(KEY_BIRTHDAY)) {
				return getString(KEY_BIRTHDAY);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setBirthday(String birthday) {
		try {
			put(KEY_BIRTHDAY, birthday);
		} catch (JSONException e) {
			logException(e);
		}
	}

	@SuppressWarnings("unchecked") // We check it coming in.
	public JSONObject getCustomData() {
		if (!isNull(KEY_CUSTOM_DATA)) {
			try {
				return getJSONObject(KEY_CUSTOM_DATA);
			} catch (JSONException e) {
				logException(e);
			}
		}
		return null;
	}

	public void setCustomData(JSONObject customData) {
		try {
			put(KEY_CUSTOM_DATA, customData);
		} catch (JSONException e) {
			logException(e);
		}
	}
}