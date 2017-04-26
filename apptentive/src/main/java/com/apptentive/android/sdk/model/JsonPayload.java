/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveLogTag;
import com.apptentive.android.sdk.encryption.Encryptor;
import com.apptentive.android.sdk.network.HttpRequestMethod;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class JsonPayload extends Payload {

	private final JSONObject jsonObject;
	private final Encryptor encryptor;

	// These three are not stored in the JSON, only the DB.

	public JsonPayload(PayloadType type) {
		super(type);
		jsonObject = new JSONObject();
		this.encryptor = null;
	}

	public JsonPayload(PayloadType type, Encryptor encryptor) {
		super(type);
		jsonObject = new JSONObject();
		this.encryptor = encryptor;
	}

	public JsonPayload(PayloadType type, String json) throws JSONException {
		super(type);
		jsonObject = new JSONObject(json);
		this.encryptor = null;
	}

	//region Data

	@Override
	public byte[] getData() {
		try {
			byte[] bytes = jsonObject.toString().getBytes();
			if (encryptor != null) {
				return encryptor.encrypt(bytes);
			} else {
				return bytes;
			}
		} catch (Exception e) {
			ApptentiveLog.e(ApptentiveLogTag.PAYLOADS, "Error encrypting payload data", e);
		}
		return null;
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

	public String getString(String key) {
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

	//region String Representation

	@Override
	public String toString() {
		return jsonObject.toString();
	}


	//endregion

	//region Getters/Setters

	public JSONObject getJsonObject() {
		return jsonObject;
	}

	@Override
	public HttpRequestMethod getHttpRequestMethod() {
		return HttpRequestMethod.PUT;
	}

	@Override
	public String getHttpRequestContentType() {
		if (encryptor != null) {
			return "application/json";
		} else {
			return "application/octet-stream";
		}
	}

	//endregion
}
