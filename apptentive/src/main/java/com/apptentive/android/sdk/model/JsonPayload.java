/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.encryption.Encryptor;
import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import static com.apptentive.android.sdk.ApptentiveLogTag.PAYLOADS;

public abstract class JsonPayload extends Payload {

	private static final String KEY_NONCE = "nonce";

	private final JSONObject jsonObject;

	public JsonPayload(PayloadType type) {
		super(type);
		jsonObject = new JSONObject();
		setNonce(UUID.randomUUID().toString());
	}

	public JsonPayload(PayloadType type, String json) throws JSONException {
		super(type);
		jsonObject = new JSONObject(json);
	}

	//region Data

	@Override
	public byte[] renderData() throws JSONException {
		String jsonString = marshallForSending().toString();
		ApptentiveLog.v(PAYLOADS, jsonString);

		if (encryptionKey != null) {
			byte[] bytes = jsonString.getBytes();
			Encryptor encryptor = new Encryptor(encryptionKey);
			try {
				return encryptor.encrypt(bytes);
			} catch (Exception e) {
				ApptentiveLog.e(PAYLOADS, "Error encrypting payload data", e);
			}
			return null;
		} else {
			return jsonString.getBytes();
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

	public String optString(String key, String fallback) {
		if (!jsonObject.isNull(key)) {
			return jsonObject.optString(key, fallback);
		}
		return null;
	}

	public int optInt(String key, int defaultValue) {
		return jsonObject.optInt(key, defaultValue);
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		return jsonObject.optBoolean(key, defaultValue);
	}

	protected Double getDouble(String key) {
		try {
			return jsonObject.getDouble(key);
		} catch (Exception e) {
			// Ignore.
		}
		return null;
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
		return StringUtils.format("%s %s", getClass().getSimpleName(), jsonObject);
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

	@Override
	public String getNonce() {
		return optString(KEY_NONCE, null);
	}

	@Override
	public void setNonce(String nonce) {
		put(KEY_NONCE, nonce);
	}

	//endregion

	protected final JSONObject marshallForSending() throws JSONException {
		JSONObject result;
		String container = getJsonContainer();
		if (container != null) {
			result = new JSONObject();
			result.put(container, jsonObject);
		} else {
			result = jsonObject;
		}

		if (encryptionKey != null) {
			result.put("token", token);
		}

		return result;
	}

	protected String getJsonContainer() {
		return null;
	}
}
