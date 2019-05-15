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

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class IntegrationConfigItem implements Serializable {
	private static final long serialVersionUID = 3509802144209212980L;
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
			return ret;
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		IntegrationConfigItem that = (IntegrationConfigItem) o;

		return contents != null ? contents.equals(that.contents) : that.contents == null;

	}

	@Override
	public int hashCode() {
		return contents != null ? contents.hashCode() : 0;
	}

	// TODO: unit testing
	public IntegrationConfigItem clone() {
		IntegrationConfigItem clone = new IntegrationConfigItem();
		clone.contents.putAll(contents);
		return clone;
	}
}
