/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import org.json.JSONException;
import org.json.JSONObject;

public class VersionHistoryEntry extends JSONObject {

	private static final String KEY_VERSION_CODE = "versionCode";
	private static final String KEY_VERSION_NAME = "versionName";
	private static final String KEY_TIMESTAMP = "timestamp";

	VersionHistoryEntry(JSONObject jsonObject) throws JSONException {
		this(jsonObject.toString());
	}

	private VersionHistoryEntry(String json) throws JSONException {
		super(json);
	}

	VersionHistoryEntry(Integer versionCode, String versionName, Double timestamp) throws JSONException {
		setVersionCode(versionCode);
		setVersionName(versionName);
		setTimestamp(timestamp);
	}

	public int getVersionCode() {
		return optInt(KEY_VERSION_CODE);
	}

	public void setVersionCode(Integer versionCode) throws JSONException {
		put(KEY_VERSION_CODE, versionCode);
	}

	public String getVersionName() {
		return optString(KEY_VERSION_NAME, null);
	}

	public void setVersionName(String versionName) throws JSONException {
		put(KEY_VERSION_NAME, versionName);
	}

	public Double getTimestamp() {
		return (Double) opt(KEY_TIMESTAMP);
	}

	public void setTimestamp(Double timestamp) throws JSONException {
		put(KEY_TIMESTAMP, timestamp);
	}
}
