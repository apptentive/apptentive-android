/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class Person extends JSONObject {

	public static final String KEY = "person";

	private static final String KEY_ID = "id";
	private static final String KEY_EMAIL = "email";
	private static final String KEY_FACEBOOK_ID = "facebook_id";
	private static final String KEY_PHONE_NUMBER = "phone_number";
	private static final String KEY_STREET = "street";
	private static final String KEY_CITY = "city";
	private static final String KEY_ZIP = "zip";
	private static final String KEY_COUNTRY = "country";
	private static final String KEY_BIRTHDAY = "birthday";

	public Person(String json) throws JSONException {
		super(json);
	}

	public Person() {
	}

	public String getId() {
		try {
			return getString(KEY_ID);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setId(String id) {
		try {
			put(KEY_ID, id);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_ID + " of " + KEY);
		}
	}

	public String getEmail() {
		try {
			return getString(KEY_EMAIL);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setEmail(String email) {
		try {
			put(KEY_EMAIL, email);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_EMAIL + " of " + KEY);
		}
	}

	public String getFacebookId() {
		try {
			return getString(KEY_FACEBOOK_ID);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setFacebookId(String facebookId) {
		try {
			put(KEY_FACEBOOK_ID, facebookId);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_FACEBOOK_ID + " of " + KEY);
		}
	}

	public String getPhoneNumber(String phoneNumber) {
		try {
			return getString(KEY_PHONE_NUMBER);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setPhoneNumber(String phoneNumber) {
		try {
			put(KEY_PHONE_NUMBER, phoneNumber);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_PHONE_NUMBER + " of " + KEY);
		}
	}

	public String getStreet() {
		try {
			return getString(KEY_STREET);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setstreet(String street) {
		try {
			put(KEY_STREET, street);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_STREET + " of " + KEY);
		}
	}

	public String getCity() {
		try {
			return getString(KEY_CITY);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setCity(String city) {
		try {
			put(KEY_CITY, city);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_CITY + " of " + KEY);
		}
	}

	public String getZip() {
		try {
			return getString(KEY_ZIP);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setZip(String zip) {
		try {
			put(KEY_ZIP, zip);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_ZIP + " of " + KEY);
		}
	}

	public String getCountry() {
		try {
			return getString(KEY_COUNTRY);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setCountry(String country) {
		try {
			put(KEY_COUNTRY, country);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_COUNTRY + " of " + KEY);
		}
	}

	public String getBirthday() {
		try {
			return getString(KEY_BIRTHDAY);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setBirthday(String birthday) {
		try {
			put(KEY_BIRTHDAY, birthday);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_BIRTHDAY + " of " + KEY);
		}
	}
}
