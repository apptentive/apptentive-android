/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.network.HttpRequestMethod;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Payload extends JSONObject {

	// These three are not stored in the JSON, only the DB.
	private Long databaseId; // FIXME: use 'long' instead
	private BaseType baseType;
	private String conversationId;
	private String token;

	public Payload() {
		initBaseType();
	}

	public Payload(String json) throws JSONException {
		super(json);
		initBaseType();
	}

	public Payload(String json, String conversationId, String token) throws JSONException {
		this(json);
		this.conversationId = conversationId;
		this.token = token;
	}

	/**
	 * Each subclass must set its type in this method.
	 */
	protected abstract void initBaseType();

	/**
	 * Subclasses should override this method if there is any peculiarity in how they present or wrap json before sending.
	 *
	 * @return A wrapper object containing the name of the object type, the value of which is the contents of this Object.
	 */
	public String marshallForSending() {
		JSONObject wrapper = new JSONObject();
		try {
			wrapper.put(getBaseType().name(), this);
		} catch (JSONException e) {
			ApptentiveLog.w("Error wrapping Payload in JSONObject.", e);
			return null;
		}
		return wrapper.toString();
	}

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

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public enum BaseType {
		message,
		event,
		device,
		sdk,
		app_release,
		sdk_and_app_release,
		person,
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

	/**
	 * @deprecated Do not use this method to check for key existence. Instead us !isNull(KEY_NAME), as this works better
	 * with keys with null values.
	 */
	@Override
	public boolean has(String key) {
		return super.has(key);
	}

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
