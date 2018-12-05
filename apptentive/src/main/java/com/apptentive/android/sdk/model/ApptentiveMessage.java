/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static com.apptentive.android.sdk.ApptentiveLogTag.MESSAGES;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public abstract class ApptentiveMessage extends ConversationItem implements MessageCenterListItem {

	public static final String KEY_ID = "id";
	public static final String KEY_CREATED_AT = "created_at";
	public static final String KEY_TYPE = "type";
	public static final String KEY_HIDDEN = "hidden";
	/** inbound here means inbound to the server. When this is true, the message is outgoing */
	public static final String KEY_INBOUND = "inbound";
	@SensitiveDataKey public static final String KEY_CUSTOM_DATA = "custom_data";
	public static final String KEY_AUTOMATED = "automated";
	public static final String KEY_SENDER = "sender";
	public static final String KEY_SENDER_ID = "id";
	@SensitiveDataKey private static final String KEY_SENDER_NAME = "name";
	@SensitiveDataKey private static final String KEY_SENDER_PROFILE_PHOTO = "profile_photo";

	// State and Read are not stored in JSON, only in DB.
	private State state = State.unknown;
	private boolean read = false;

	// datestamp is only stored in memory, due to how we selectively apply date labeling in the view.
	private String datestamp;

	// this an abstract class so we don't need to register it's sensitive keys (subclasses will do)

	protected ApptentiveMessage() {
		super(PayloadType.message);
		state = State.sending;
		read = true; // This message originated here.
		initType();
	}

	protected ApptentiveMessage(String json) throws JSONException {
		super(PayloadType.message, json);
		state = State.unknown;
		initType();
	}

	protected abstract void initType();

	public void setId(String id) {
		put(KEY_ID, id);
	}

	public String getId() {
		return optString(KEY_ID, null);
	}

	public Double getCreatedAt() {
		return getDouble(KEY_CREATED_AT);
	}

	public void setCreatedAt(Double createdAt) {
		put(KEY_CREATED_AT, createdAt);
	}

	public Type getMessageType() {
		if (isNull(KEY_TYPE)) {
			return Type.CompoundMessage;
		}
		String typeString = optString(KEY_TYPE, null);
		return typeString == null ? Type.unknown : Type.parse(typeString);
	}

	protected void setType(Type type) {
		put(KEY_TYPE, type.name());
	}

	public boolean isHidden() {
		return getBoolean(KEY_HIDDEN);
	}

	public void setHidden(boolean hidden) {
		put(KEY_HIDDEN, hidden);
	}

	public boolean isOutgoingMessage() {
		// Default is true because this field is only set from the server.
		return getBoolean(KEY_INBOUND, true);
	}

	public void setCustomData(Map<String, Object> customData) {
		if (customData == null || customData.size() == 0) {
			if (!isNull(KEY_CUSTOM_DATA)) {
				remove(KEY_CUSTOM_DATA);
			}
			return;
		}
		try {
			JSONObject customDataJson = new JSONObject();
			for (String key : customData.keySet()) {
				customDataJson.put(key, customData.get(key));
			}
			put(KEY_CUSTOM_DATA, customDataJson);
		} catch (JSONException e) {
			ApptentiveLog.e(e, "Exception setting ApptentiveMessage's %s field.", KEY_CUSTOM_DATA);
			logException(e);
		}
	}

	public State getState() {
		if (state == null) {
			return State.unknown;
		}
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	// For debugging only.
	public void setSenderId(String senderId) {
		try {
			JSONObject sender;
			if (!isNull((KEY_SENDER))) {
				sender = getJSONObject(KEY_SENDER);
			} else {
				sender = new JSONObject();
				put(KEY_SENDER, sender);
			}
			sender.put(KEY_SENDER_ID, senderId);
		} catch (JSONException e) {
			ApptentiveLog.e(e, "Exception setting ApptentiveMessage's %s field.", KEY_SENDER_ID);
			logException(e);
		}
	}

	public String getSenderUsername() {
		try {
			if (!isNull((KEY_SENDER))) {
				JSONObject sender = getJSONObject(KEY_SENDER);
				if (!sender.isNull((KEY_SENDER_NAME))) {
					return sender.getString(KEY_SENDER_NAME);
				}
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public String getSenderProfilePhoto() {
		try {
			if (!isNull((KEY_SENDER))) {
				JSONObject sender = getJSONObject(KEY_SENDER);
				if (!sender.isNull((KEY_SENDER_PROFILE_PHOTO))) {
					return sender.getString(KEY_SENDER_PROFILE_PHOTO);
				}
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public boolean getAutomated() {
		return getBoolean(KEY_AUTOMATED);
	}

	public void setAutomated(boolean isAutomated) {
		put(KEY_AUTOMATED, isAutomated);
	}

	public String getDatestamp() {
		return datestamp;
	}

	/**
	 * Sets the datestamp for this message.
	 *
	 * @param datestamp A datestamp
	 * @return true if the datestamp was added or changed.
	 */
	public boolean setDatestamp(String datestamp) {
		if (this.datestamp == null || !this.datestamp.equals(datestamp)) {
			this.datestamp = datestamp;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Clears the datestamp from a message
	 *
	 * @return true If the datestamp existed and was cleared, false if it was already cleared.
	 */
	public boolean clearDatestamp() {
		if (datestamp != null) {
			this.datestamp = null;
			return true;
		} else {
			return false;
		}
	}

	public boolean isAutomatedMessage() {
		return getAutomated();
	}

	public enum Type {
		TextMessage,
		FileMessage,
		AutomatedMessage,
		CompoundMessage,
		// Unknown
		unknown;

		public static Type parse(String rawType) {
			try {
				return Type.valueOf(rawType);
			} catch (IllegalArgumentException e) {
				ApptentiveLog.v(MESSAGES, "Error parsing unknown ApptentiveMessage.Type: " + rawType);
				logException(e);
			}
			return unknown;
		}
	}

	public enum State {
		sending, // The item is either being sent, or is queued for sending.
		sent,    // The item has been posted to the server successfully.
		saved,   // The item has been returned from the server during a fetch.
		unknown;

		public static State parse(String state) {
			try {
				return State.valueOf(state);
			} catch (IllegalArgumentException e) {
				ApptentiveLog.v(MESSAGES, "Error parsing unknown ApptentiveMessage.State: " + state);
				logException(e);
			}
			return unknown;
		}
	}
}
