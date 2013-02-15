/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public abstract class Message extends ActivityFeedItem {

	public static final int DEFAULT_PRIORITY = 10;

	private static final String KEY_SENDER = "sender";
	private static final String KEY_SENDER_ID = "id";
	private static final String KEY_SENDER_NAME = "name";
	private static final String KEY_SENDER_PROFILE_PHOTO = "profile_photo";
	private static final String KEY_PRIORITY = "priority";
	private static final String KEY_DISPLAY = "display";
	private static final String KEY_USER_VISIBLE = "user_visible";
	private static final String KEY_READ = "read";

	protected Message() {
		super();
		setSenderId(GlobalInfo.personId);
	}

	protected Message(String json) throws JSONException {
		super(json);
	}

	protected abstract void initType();

	public String getSenderId() {
		try {
			if (!isNull((KEY_SENDER))) {
				JSONObject sender = getJSONObject(KEY_SENDER);
				if (!sender.isNull((KEY_SENDER_ID))) {
					return sender.getString(KEY_SENDER_ID);
				}
			}
		} catch (JSONException e) {
		}
		return null;
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
			Log.e("Exception setting Message's %s field.", e, KEY_SENDER_ID);
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
			// Should not happen.
		}
		return null;
	}

	public Integer getPriority() {
		try {
			if (!isNull((KEY_PRIORITY))) {
				return getInt(KEY_PRIORITY);
			}
		} catch (JSONException e) {
		}
		return DEFAULT_PRIORITY;
	}

	public String getDisplay() {
		try {
			if (!isNull((KEY_DISPLAY))) {
				return getString(KEY_DISPLAY);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public boolean isUserVisible() {
		try {
			return getBoolean(KEY_USER_VISIBLE);
		} catch (JSONException e) {
		}
		return false; // Unsupported.
	}

	public boolean isOutgoingMessage() {
		String senderId = getSenderId();
		boolean outgoing = senderId == null || senderId.equals(GlobalInfo.personId) || getState().equals(State.sending);
		return outgoing;
	}
}
