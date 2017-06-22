/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

public class IntegrationConfigItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String KEY_TOKEN = "token";

	private HashMap<String, String> contents = new HashMap<>();

	public IntegrationConfigItem() {
	}

	public IntegrationConfigItem(JSONObject old) {
		String oldToken = old.optString(KEY_TOKEN, null);
		setToken(oldToken);
	}

	public void setToken(String token) {
		contents.put(KEY_TOKEN, token);
	}

	public com.apptentive.android.sdk.model.CustomData toJson() {
		try {
			com.apptentive.android.sdk.model.CustomData ret = new com.apptentive.android.sdk.model.CustomData();
			Set<String> keys = contents.keySet();
			for (String key : keys) {
				ret.put(key, contents.get(key));
			}
		} catch (JSONException e) {
			// This can't happen.
		}
		return null;
	}
}
