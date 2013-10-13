/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.apptentive.android.sdk.model.CustomData;
import com.apptentive.android.sdk.model.Person;
import com.apptentive.android.sdk.util.Constants;
import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class PersonManager {

	public static Person storePersonAndReturnDiff(Context context) {
		Person original = getStoredPerson(context);

		Person current = generateCurrentPerson();
		CustomData customData = loadCustomPersonData(context);
		current.setCustomData(customData);
		String email = loadPersonEmail(context);
		if (email == null) {
			email = loadInitialPersonEmail(context);
		}
		current.setEmail(email);

		Person diff = diffPerson(original, current);
		if (diff != null) {
			storePerson(context, current);
			return diff;
		}

		return null;
	}

	public static CustomData loadCustomPersonData(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String personDataString = prefs.getString(Constants.PREF_KEY_PERSON_DATA, null);
		try {
			return new CustomData(personDataString);
		} catch (Exception e) {
		}
		try {
			return new CustomData();
		} catch (JSONException e) {
		}
		return null;
	}

	public static void storeCustomPersonData(Context context, CustomData deviceData) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String personDataString = deviceData.toString();
		prefs.edit().putString(Constants.PREF_KEY_PERSON_DATA, personDataString).commit();
	}

	private static Person generateCurrentPerson() {
		return new Person();
	}

	public static String loadInitialPersonEmail(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		return prefs.getString(Constants.PREF_KEY_PERSON_INITIAL_EMAIL, null);
	}

	public static void storeInitialPersonEmail(Context context, String email) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_PERSON_INITIAL_EMAIL, email).commit();
	}

	public static String loadPersonEmail(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		return prefs.getString(Constants.PREF_KEY_PERSON_EMAIL, null);
	}

	public static void storePersonEmail(Context context, String email) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_PERSON_EMAIL, email).commit();
	}

	public static Person getStoredPerson(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String PersonString = prefs.getString(Constants.PREF_KEY_PERSON, null);
		try {
			return new Person(PersonString);
		} catch (Exception e) {
		}
		return null;
	}

	private static void storePerson(Context context, Person Person) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_PERSON, Person.toString()).commit();
	}

	private static Person diffPerson(Person old, Person newer) {
		if (old == null) {
			return newer;
		}

		Person ret = new Person();
		int baseEntries = ret.length();

		String id = chooseLatest(old.getId(), newer.getId());
		if (id != null) {
			ret.setId(id);
		}

		String email = chooseLatest(old.getEmail(), newer.getEmail());
		if (email != null) {
			ret.setEmail(email);
		}

		String facebookId = chooseLatest(old.getFacebookId(), newer.getFacebookId());
		if (facebookId != null) {
			ret.setFacebookId(facebookId);
		}

		String phoneNumber = chooseLatest(old.getPhoneNumber(), newer.getPhoneNumber());
		if (phoneNumber != null) {
			ret.setPhoneNumber(phoneNumber);
		}

		String street = chooseLatest(old.getStreet(), newer.getStreet());
		if (street != null) {
			ret.setStreet(street);
		}

		String city = chooseLatest(old.getCity(), newer.getCity());
		if (city != null) {
			ret.setCity(city);
		}

		String zip = chooseLatest(old.getZip(), newer.getZip());
		if (zip != null) {
			ret.setZip(zip);
		}

		String country = chooseLatest(old.getCountry(), newer.getCountry());
		if (country != null) {
			ret.setCountry(country);
		}

		String birthday = chooseLatest(old.getBirthday(), newer.getBirthday());
		if (birthday != null) {
			ret.setBirthday(birthday);
		}

		CustomData customData = chooseLatest(old.getCustomData(), newer.getCustomData());
		if (customData != null) {
			ret.setCustomData(customData);
		}

		// If there were no differences, return null.
		if (ret.length() <= baseEntries) {
			return null;
		}
		return ret;
	}

	/**
	 * A convenience method.
	 *
	 * @return newer - if it is different from old. <p/>empty string - if there was an old value, but not a newer value. This clears the old value.<p/> null - if there is no difference.
	 */
	private static String chooseLatest(String old, String newer) {
		if (old == null || old.equals("")) {
			old = null;
		}
		if (newer == null || newer.equals("")) {
			newer = null;
		}

		// New value.
		if (old != null && newer != null && !old.equals(newer)) {
			return newer;
		}

		// Clear existing value.
		if (old != null && newer == null) {
			return "";
		}

		if (old == null && newer != null) {
			return newer;
		}

		// Do nothing.
		return null;
	}

	private static CustomData chooseLatest(CustomData old, CustomData newer) {
		if (old == null || old.length() == 0) {
			old = null;
		}
		if (newer == null || newer.length() == 0) {
			newer = null;
		}

		// New value.
		if (old != null && newer != null && !old.equals(newer)) {
			return newer;
		}

		// Clear existing value.
		if (old != null && newer == null) {
			try {
				return new CustomData();
			} catch (JSONException e) {
				return null;
			}
		}

		if (old == null && newer != null) {
			return newer;
		}

		// Do nothing.
		return null;
	}
}
