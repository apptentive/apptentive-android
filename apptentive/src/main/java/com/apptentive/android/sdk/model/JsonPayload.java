/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveLogTag;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public abstract class JsonPayload extends Payload {

	private final JSONObject jsonObject;

	// These three are not stored in the JSON, only the DB.

	public JsonPayload() {
		super();
		jsonObject = new JSONObject();
		initPayloadType();
	}

	public JsonPayload(String json) {
		super();
		JSONObject temp = null;
		try {
			temp = new JSONObject(json);
		} catch (JSONException e) {
			ApptentiveLog.e(ApptentiveLogTag.PAYLOADS, "Error creating JsonPayload from json string.", e);
		}
		jsonObject = (temp == null ? null : temp);
		initPayloadType();
	}

	//region Data

	@Override
	public byte[] getData() {
		try {
			return jsonObject.toString().getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	//endregion

	//region Json

	protected void put(String key, String value) {
		try {
			jsonObject.put(key, value);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while putting json pair '%s'='%s'", key, value);
		}
	}

	protected void put(String key, boolean value) {
		try {
			jsonObject.put(key, value);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while putting json pair '%s'='%s'", key, value);
		}
	}

	protected void put(String key, int value) {
		try {
			jsonObject.put(key, value);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while putting json pair '%s'='%s'", key, value);
		}
	}

	protected void put(String key, double value) {
		try {
			jsonObject.put(key, value);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while putting json pair '%s'='%s'", key, value);
		}
	}

	protected void put(String key, JSONObject object) {
		try {
			jsonObject.put(key, object);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while putting json pair '%s'='%s'", key, object);
		}
	}

	protected void remove(String key) { // TODO: rename to removeKey
		jsonObject.remove(key);
	}

	protected String getString(String key) {
		return jsonObject.optString(key);
	}

	public int getInt(String key) {
		return getInt(key, 0);
	}

	public int getInt(String key, int defaultValue) {
		return jsonObject.optInt(key, defaultValue);
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		return jsonObject.optBoolean(key, defaultValue);
	}

	protected double getDouble(String key) {
		return getDouble(key, 0.0);
	}

	protected double getDouble(String key, double defaultValue) {
		return jsonObject.optDouble(key, defaultValue);
	}

	protected JSONObject getJSONObject(String key) {
		return jsonObject.optJSONObject(key);
	}

	protected boolean isNull(String key) { // TODO: rename to containsKey
		return jsonObject.isNull(key);
	}

	//endregion

	//region Getters/Setters

	public JSONObject getJsonObject() {
		return jsonObject;
	}

	//endregion
}
