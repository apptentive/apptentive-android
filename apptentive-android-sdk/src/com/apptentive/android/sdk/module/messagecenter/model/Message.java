/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.offline.Payload;
import org.json.JSONException;

import java.util.Date;

/**
 * @author Sky Kelsey
 */
public class Message extends Payload {

	public static final int DEFAULT_PRIORITY = 10;

	private long id = -1; // The DB id of this stored object. Not stored in JSON.
	private static final String KEY_MESSAGE_ID = "id";
	protected static final String KEY_CREATED_AT = "created_at";
	private static final String KEY_SENDER_ID = "sender_id";
	private static final String KEY_PRIORITY = "priority";
	private static final String KEY_TYPE = "type";
	private static final String KEY_DISPLAY = "display";

	public Message() {
		super();
		setCreatedAt(((double) new Date().getTime()) / 1000);
	}

	public Message(String json) throws JSONException {
		super(json);
	}

	public PayloadType getPayloadType() {
		return PayloadType.MESSAGE;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMessageId() {
		try {
			if (has((KEY_MESSAGE_ID))) {
				return getString(KEY_MESSAGE_ID);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public Double getCreatedAt() {
		try {
			if (has((KEY_CREATED_AT))) {
				return getDouble(KEY_CREATED_AT);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public void setCreatedAt(Double createdAt) {
		try {
			put(KEY_CREATED_AT, createdAt);
		} catch (JSONException e) {
			Log.e("Exception setting Message's %s field.", e, KEY_CREATED_AT);
		}
	}

	public String getSenderId() {
		try {
			if (has((KEY_SENDER_ID))) {
				return getString(KEY_SENDER_ID);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public Integer getPriority() {
		try {
			if (has((KEY_PRIORITY))) {
				return getInt(KEY_PRIORITY);
			}
		} catch (JSONException e) {
		}
		return DEFAULT_PRIORITY;
	}

	public String getType() {
		try {
			if (has((KEY_TYPE))) {
				return getString(KEY_TYPE);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public MessageType getTypeEnum() {

		try {
			if (has(KEY_TYPE)) {
				return MessageType.valueOf(getString(KEY_TYPE));
			}
		} catch (JSONException e) {
		} catch (IllegalArgumentException e) {
		}
		return MessageType.unknown;
	}

	public void setType(MessageType type) {
		try {
			put(KEY_TYPE, type.name());
		} catch (JSONException e) {
			Log.e("Exception setting Message's %s field.", e, KEY_TYPE);
		}
	}


	public String getDisplay() {
		try {
			if (has((KEY_DISPLAY))) {
				return getString(KEY_DISPLAY);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public boolean isOutgoingMessage() {
		String senderId = getSenderId();
		return senderId == null || senderId.equals(GlobalInfo.personId);
	}

	public enum MessageType {
		text_message,
		upgrade_request,
		share_request,
		unknown
	}
}
