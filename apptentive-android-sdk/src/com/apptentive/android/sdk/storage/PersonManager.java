/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.model.Person;
import com.apptentive.android.sdk.util.Constants;

/**
 * @author Sky Kelsey
 */
public class PersonManager {

	public static Person storePersonAndReturnDiff(Context context) {
		Person original = getStoredPerson(context);
		Person current = generateCurrentPerson();
		Person diff = diffPerson(original, current);
		if (diff != null) {
			storePerson(context, current);
			return diff;
		}
		return null;
	}

	private static Person generateCurrentPerson() {
		Person person = new Person();
		person.setEmail(GlobalInfo.userEmail);
		return person;
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

	private static Person diffPerson(Person older, Person newer) {
		if (older == null) {
			return newer;
		}

		Person ret = new Person();
		int baseEntries = ret.length();

		String id = chooseLatest(older.getId(), newer.getId());
		if (id != null) {
			ret.setId(id);
		}

		String email = chooseLatest(older.getEmail(), newer.getEmail());
		if (email != null) {
			ret.setEmail(email);
		}

		String facebookId = chooseLatest(older.getFacebookId(), newer.getFacebookId());
		if (facebookId != null) {
			ret.setFacebookId(facebookId);
		}

		String phoneNumber = chooseLatest(older.getPhoneNumber(), newer.getPhoneNumber());
		if (phoneNumber != null) {
			ret.setPhoneNumber(phoneNumber);
		}

		String street = chooseLatest(older.getStreet(), newer.getStreet());
		if (street != null) {
			ret.setStreet(street);
		}

		String city = chooseLatest(older.getCity(), newer.getCity());
		if (city != null) {
			ret.setCity(city);
		}

		String zip = chooseLatest(older.getZip(), newer.getZip());
		if (zip != null) {
			ret.setZip(zip);
		}

		String country = chooseLatest(older.getCountry(), newer.getCountry());
		if (country != null) {
			ret.setCountry(country);
		}

		String birthday = chooseLatest(older.getBirthday(), newer.getBirthday());
		if (birthday != null) {
			ret.setBirthday(birthday);
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
}
