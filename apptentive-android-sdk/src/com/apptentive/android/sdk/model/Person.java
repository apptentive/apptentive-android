/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
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
	private static final String KEY_PERSON = "person";
	private static final String KEY_PERSON_ID = "id";

	public Person() {
		try {
			put("person", new JSONObject());
		} catch (JSONException e) {
			// Can't happen.
			Log.e("Unable to construct Person.", e);
		}
	}

	public String getId() {
		try {
			return getJSONObject(KEY_PERSON).getString(KEY_PERSON_ID);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setId(String id) {
		try {
			getJSONObject(KEY_PERSON).put(KEY_PERSON_ID, id);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_PERSON_ID + " of " + KEY_PERSON);
		}
	}
}
