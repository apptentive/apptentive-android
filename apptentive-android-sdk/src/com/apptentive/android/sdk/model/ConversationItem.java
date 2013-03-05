/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

/**
 * @author Sky Kelsey
 */
public abstract class ConversationItem extends JSONObject {

	private Long databaseId;
	private State state;

	public static final String KEY_ID = "id";
	public static final String KEY_TYPE = "type";
	public static final String KEY_CREATED_AT = "created_at";
	protected static final String KEY_CLIENT_CREATED_AT = "client_created_at";
	protected static final String KEY_NONCE = "nonce";

	public ConversationItem() {
		initType();
		long millis = new Date().getTime();
		Log.e("millis: " + millis);
		double point = (double)millis;
		Log.e("point: " + point);
		double seconds = point / 1000;
		Log.e("stamp: " + seconds);

		setClientCreatedAt(seconds);
		setNonce(UUID.randomUUID().toString());
		state = State.sending;
	}

	public ConversationItem(String json) throws JSONException {
		super(json);
	}

	/**
	 * Each subclass must set its type in this method.
	 */
	protected abstract void initType();

	/**
	 * Subclasses should override this method if there is any peculiarity in how they present or wrap json before sending.
	 * @return
	 */
	public String marshallForSending() {
		JSONObject wrapper = new JSONObject();
		try {
			wrapper.put(getBaseType().name(), this);
		} catch (JSONException e) {
			Log.w("Error wrapping Record in JSONObject.", e);
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

	public State getState() {
		if(state == null) {
			return State.unknown;
		}
		return state;
	}

	public void setState(
			State state) {
		this.state = state;
	}

	public void setId(String id) {
		try {
			put(KEY_ID, id);
		} catch (JSONException e) {
			Log.e("Exception setting ConversationItem's %s field.", e, KEY_ID);
		}
	}

	public String getId() {
		try {
			if (!isNull((KEY_ID))) {
				return getString(KEY_ID);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public Type getType() {
		try {
			return Type.parse(getString(KEY_TYPE));
		} catch (JSONException e) {
		}
		return Type.unknown;
	}

	protected void setType(Type type) {
		try {
			put(KEY_TYPE, type.name());
		} catch (JSONException e) {
			Log.e("Exception setting ConversationItem's %s field.", e, KEY_TYPE);
		}
	}

	public BaseType getBaseType() {
		return getType().getBaseType();
	}

	protected void setNonce(String nonce) {
		try {
			put(KEY_NONCE, nonce);
		} catch (JSONException e) {
			Log.e("Exception setting ConversationItem's %s field.", e, KEY_NONCE);
		}
	}

	public String getNonce() {
		try {
			if (!isNull((KEY_NONCE))) {
				return getString(KEY_NONCE);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public Double getCreatedAt() {
		try {
			return getDouble(KEY_CREATED_AT);
		} catch (JSONException e) {
		}
		return null;
	}

	public void setCreatedAt(Double createdAt) {
		try {
			put(KEY_CREATED_AT, createdAt);
		} catch (JSONException e) {
			Log.e("Exception setting ConversationItem's %s field.", e, KEY_CREATED_AT);
		}
	}

	public Double getClientCreatedAt() {
		try {
			return getDouble(KEY_CLIENT_CREATED_AT);
		} catch (JSONException e) {
		}
		return null;
	}

	private void setClientCreatedAt(Double clientCreatedAt) {
		try {
			put(KEY_CLIENT_CREATED_AT, clientCreatedAt);
		} catch (JSONException e) {
			Log.e("Exception setting ConversationItem's %s field.", e, KEY_CLIENT_CREATED_AT);
		}
	}

	public enum Type {
		// Message
		TextMessage,
		FileMessage,

		// Event
		Event,

		// Device
		Device,

		// Legacy
		feedback,
		survey,

		// Unknown
		unknown;

		public static Type parse(String rawType) {
			try {
				return Type.valueOf(rawType);
			} catch (IllegalArgumentException e) {
				Log.v("Error parsing unknown ConversationItem.Type: " + rawType);
			}
			return unknown;
		}

		public BaseType getBaseType() {
			switch(this) {
				case TextMessage:
					return BaseType.message;
				case FileMessage:
					return BaseType.message;
				case Event:
					return BaseType.event;
				case survey:
					return BaseType.survey;
				case Device:
					return BaseType.device;
				case unknown:
					return BaseType.unknown;
				default:
					return BaseType.unknown;
			}
		}
	}

	public static enum BaseType {
		message,
		event,
		device,
		unknown,
		// Legacy
		survey;

		public static BaseType parse(String type) {
			try {
				return BaseType.valueOf(type);
			} catch (IllegalArgumentException e) {
				Log.v("Error parsing unknown ConversationItem.BaseType: " + type);
			}
			return unknown;
		}

	}

	public static enum State {
		sending, // The item is either being sent, or is queued for sending.
		sent,    // The item has been posted to the server successfully.
		saved,   // The item has been returned from the server during a fetch.
		unknown;

		public static State parse(String state) {
			try {
				return State.valueOf(state);
			} catch (IllegalArgumentException e) {
				Log.v("Error parsing unknown ConversationItem.State: " + state);
			}
			return unknown;
		}
	}

	/**
	 * @deprecated Do not use this method to check for key existence. Instead us !isNull(KEY_NAME), as this works better
	 * with keys with null values.
	 * @param key
	 * @return
	 */
	@Override
	public boolean has(String key) {
		return super.has(key);
	}
}
