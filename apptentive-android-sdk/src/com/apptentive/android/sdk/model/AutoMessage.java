/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Log;
import org.json.JSONException;

/**
 * A dummy Message that the client can create on behalf of the server. It will never be sent to the server.
 * @author Sky Kelsey
 */
public class AutoMessage extends Message {

	private static final String KEY_TITLE = "title";
	private static final String KEY_BODY = "body";
	// This is used to determine the ordering of this message, since the server will never have a chance to assign it an id.
	private static final String KEY_PREVIOUS_MESSAGE_ID = "previous_message_id";

	public AutoMessage() {
		super();
	}

	public AutoMessage(String json) throws JSONException {
		super(json);
	}

	@Override
	protected void initType() {
		setType(Type.AutoMessage);
	}

	public String getTitle() {
		try {
			return getString(KEY_TITLE);
		}catch (JSONException e) {
		}
		return null;
	}

	public void setTitle(String title) {
		try {
			put(KEY_TITLE, title);
		} catch (JSONException e) {
			Log.e("Unable to set title.");
		}
	}

	public String getBody() {
		try {
			return getString(KEY_BODY);
		}catch (JSONException e) {
		}
		return null;
	}

	public void setBody(String body) {
		try {
			put(KEY_BODY, body);
		} catch (JSONException e) {
			Log.e("Unable to set body.");
		}
	}

	public String getPreviousMessageId() {
		try {
			return getString(KEY_PREVIOUS_MESSAGE_ID);
		}catch (JSONException e) {
		}
		return null;
	}

	public void setPreviousMessageId(String previousMessageId) {
		try {
			put(KEY_BODY, previousMessageId);
		} catch (JSONException e) {
			Log.e("Unable to set previous message id.");
		}
	}
}
