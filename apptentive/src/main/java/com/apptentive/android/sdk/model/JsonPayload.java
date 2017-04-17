/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.network.HttpRequestMethod;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public abstract class JsonPayload extends Payload {

	private final JSONObject jsonObject;

	// These three are not stored in the JSON, only the DB.
	private long databaseId;
	private BaseType baseType;
	private String conversationId;

	public JsonPayload() {
		jsonObject = new JSONObject();
		initBaseType();
	}

	/**
	 * Each subclass must set its type in this method.
	 */
	protected abstract void initBaseType();

	public long getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(long databaseId) {
		this.databaseId = databaseId;
	}

	public BaseType getBaseType() {
		return baseType;
	}

	protected void setBaseType(BaseType baseType) {
		this.baseType = baseType;
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public enum BaseType {
		message,
		event,
		device,
		sdk,
		app_release,
		sdk_and_app_release,
		person,
		logout,
		unknown,
		// Legacy
		survey;

		public static BaseType parse(String type) {
			try {
				return BaseType.valueOf(type);
			} catch (IllegalArgumentException e) {
				ApptentiveLog.v("Error parsing unknown Payload.BaseType: " + type);
			}
			return unknown;
		}

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

	//region Http-request

	/**
	 * Http endpoint for sending this payload
	 */
	public abstract String getHttpEndPoint();

	/**
	 * Http request method for sending this payload
	 */
	public abstract HttpRequestMethod getHttpRequestMethod();

	/**
	 * Http content type for sending this payload
	 */
	public abstract String getHttpRequestContentType();

	//endregion
}
