/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import androidx.annotation.NonNull;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.util.RuntimeUtils;
import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.apptentive.android.sdk.ApptentiveLogTag.PAYLOADS;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public abstract class JsonPayload extends Payload {

	private static final Map<Class<? extends JsonPayload>, List<String>> SENSITIVE_KEYS_LOOKUP = new HashMap<>();

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
	public @NonNull byte[] renderData() throws Exception {
		String jsonString = marshallForSending().toString();
		ApptentiveLog.v(PAYLOADS, jsonString);

		// authenticated payloads get encrypted before sending
		if (isAuthenticated()) {
			byte[] bytes = jsonString.getBytes();
			return getEncryption().encrypt(bytes);
		}

		return jsonString.getBytes();
	}

	//endregion

	//region Json

	protected void put(String key, String value) {
		try {
			jsonObject.put(key, toNullableValue(value));
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while putting json pair '%s'='%s'", key, value);
			logException(e);
		}
	}

	protected void put(String key, boolean value) {
		try {
			jsonObject.put(key, value);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while putting json pair '%s'='%s'", key, value);
			logException(e);
		}
	}

	protected void put(String key, int value) {
		try {
			jsonObject.put(key, value);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while putting json pair '%s'='%s'", key, value);
			logException(e);
		}
	}

	protected void put(String key, double value) {
		try {
			jsonObject.put(key, value);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while putting json pair '%s'='%s'", key, value);
			logException(e);
		}
	}

	protected void put(String key, JSONObject object) {
		try {
			jsonObject.put(key, toNullableValue(object));
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while putting json pair '%s'='%s'", key, object);
			logException(e);
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
			logException(e);
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

	private Object toNullableValue(Object value) {
		return value != null ? value : JSONObject.NULL;
	}

	//endregion

	//region String Representation

	@Override
	public String toString() {
		if (ApptentiveLog.shouldSanitizeLogMessages()) {
			JSONObject safeJsonObject = createSafeJsonObject(jsonObject);
			return StringUtils.format("%s %s", getClass().getSimpleName(), safeJsonObject);
		}
		return StringUtils.format("%s %s", getClass().getSimpleName(), jsonObject);
	}

	private JSONObject createSafeJsonObject(JSONObject jsonObject) {
		try {
			List<String> sensitiveKeys = SENSITIVE_KEYS_LOOKUP.get(getClass());
			if (sensitiveKeys != null && sensitiveKeys.size() > 0) {
				JSONObject safeObject = new JSONObject();
				Iterator<String> iterator = jsonObject.keys();
				while (iterator.hasNext()) {
					String key = iterator.next();
					Object value = sensitiveKeys.contains(key) ? "<HIDDEN>" : jsonObject.get(key);
					safeObject.put(key, value);
				}
				return safeObject;
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while creating safe json object");
			logException(e);
		}

		return null;
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
		if (isAuthenticated()) {
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

	final JSONObject marshallForSending() throws JSONException {
		JSONObject result;
		String container = getJsonContainer();
		if (container != null) {
			result = new JSONObject();
			result.put(container, jsonObject);
		} else {
			result = jsonObject;
		}

		if (isAuthenticated()) {
			result.put("token", getConversationToken());
		}

		if (hasSessionId()) {
			result.put("session_id", getSessionId());
		}

		return result;
	}

	protected String getJsonContainer() {
		return null;
	}

	//region Sensitive Keys

	protected static void registerSensitiveKeys(Class<? extends JsonPayload> cls) {
		List<Field> fields = RuntimeUtils.listFields(cls, new RuntimeUtils.FieldFilter() {
			@Override
			public boolean accept(Field field) {
				return Modifier.isStatic(field.getModifiers()) && // static fields
						field.getAnnotation(SensitiveDataKey.class) != null &&  // marked as 'sensitive'
						field.getType().equals(String.class); // with type of String
			}
		});

		if (fields.size() > 0) {
			List<String> keys = new ArrayList<>(fields.size());
			try {
				for (Field field : fields) {
					field.setAccessible(true);
					String value = (String) field.get(null);
					keys.add(value);
				}
				SENSITIVE_KEYS_LOOKUP.put(cls, keys);
			} catch (Exception e) {
				ApptentiveLog.e(e, "Exception while registering sensitive keys");
				logException(e);
			}
		}
	}

	//endregion
}
