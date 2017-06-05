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

	public JsonPayload(PayloadType type) {
		super(type);
		jsonObject = new JSONObject();
	}

	public JsonPayload(PayloadType type, String json) throws JSONException {
		super(type);
		jsonObject = new JSONObject(json);
	}

	//region Data

	@Override
	public byte[] renderData() {
		if (encryptionKey != null) {
			byte[] bytes = marshallForSending().toString().getBytes();
			Encryptor encryptor = new Encryptor(encryptionKey);
			ApptentiveLog.v(ApptentiveLogTag.PAYLOADS, "Getting data for encrypted payload.");
			try {
				return encryptor.encrypt(bytes);
			} catch (Exception e) {
				ApptentiveLog.e(ApptentiveLogTag.PAYLOADS, "Error encrypting payload data", e);
			}
			return null;
		} else {
			ApptentiveLog.v(ApptentiveLogTag.PAYLOADS, "Getting data for plaintext payload.");
			return marshallForSending().toString().getBytes();
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
		if (encryptionKey != null) {
			return "application/octet-stream";
		} else {
			return "application/json";
		}
	}

	//endregion

	protected JSONObject marshallForSending() {
		try {
			if (encryptionKey != null) {
				jsonObject.put("token", token);
			}
		} catch (Exception e) {
			// Can't happen.
		}
		return jsonObject;
	}
}
