/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.SharedPreferences;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.CustomData;
import com.apptentive.android.sdk.model.Person;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.JsonDiffer;

import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class PersonManager {

	public static Person storePersonAndReturnDiff() {
		Person stored = getStoredPerson();

		Person current = generateCurrentPerson();
		CustomData customData = loadCustomPersonData();
		current.setCustomData(customData);

		String email = loadPersonEmail();
		current.setEmail(email);

		String name = loadPersonName();
		current.setName(name);

		Object diff = JsonDiffer.getDiff(stored, current);
		if (diff != null) {
			try {
				storePerson(current);
				return new Person(diff.toString());
			} catch (JSONException e) {
				ApptentiveLog.e("Error casting to Person.", e);
			}
		}

		return null;
	}

	/**
	 * Provided so we can be sure that the person we send during conversation creation is 100% accurate. Since we do not
	 * queue this person up in the payload queue, it could otherwise be lost.
	 */
	public static Person storePersonAndReturnIt() {
		Person current = generateCurrentPerson();

		CustomData customData = loadCustomPersonData();
		current.setCustomData(customData);

		String email = loadPersonEmail();
		current.setEmail(email);

		String name = loadPersonName();
		current.setName(name);

		storePerson(current);
		return current;
	}

	public static CustomData loadCustomPersonData() {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		String personDataString = prefs.getString(Constants.PREF_KEY_PERSON_DATA, null);
		try {
			return new CustomData(personDataString);
		} catch (Exception e) {
			// Ignore
		}
		try {
			return new CustomData();
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public static void storeCustomPersonData(CustomData deviceData) {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		String personDataString = deviceData.toString();
		prefs.edit().putString(Constants.PREF_KEY_PERSON_DATA, personDataString).apply();
	}

	private static Person generateCurrentPerson() {
		return new Person();
	}

	public static String loadPersonEmail() {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		return prefs.getString(Constants.PREF_KEY_PERSON_EMAIL, null);
	}

	public static void storePersonEmail(String email) {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		prefs.edit().putString(Constants.PREF_KEY_PERSON_EMAIL, email).apply();
	}

	public static String loadPersonName() {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		return prefs.getString(Constants.PREF_KEY_PERSON_NAME, null);
	}

	public static void storePersonName(String name) {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		prefs.edit().putString(Constants.PREF_KEY_PERSON_NAME, name).apply();
	}

	public static Person getStoredPerson() {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		String PersonString = prefs.getString(Constants.PREF_KEY_PERSON, null);
		try {
			return new Person(PersonString);
		} catch (Exception e) {
			// Ignore
		}
		return null;
	}

	private static void storePerson(Person Person) {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		prefs.edit().putString(Constants.PREF_KEY_PERSON, Person.toString()).apply();
	}
}
