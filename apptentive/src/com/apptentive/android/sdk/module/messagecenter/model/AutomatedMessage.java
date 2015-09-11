/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import com.apptentive.android.sdk.Log;

import org.json.JSONException;

/**
 * A dummy ApptentiveMessage that the client can create on behalf of the server. It will never be sent to the server.
 *
 * @author Sky Kelsey
 */
public class AutomatedMessage extends OutgoingTextMessage {

	private static final String KEY_TITLE = "title";

	public AutomatedMessage() {
		super();
	}

	public AutomatedMessage(String json) throws JSONException {
		super(json);
	}

	@Override
	protected void initType() {
		setType(Type.AutomatedMessage);
	}

	public String getTitle() {
		try {
			return getString(KEY_TITLE);
		} catch (JSONException e) {
			// Ignore
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

	public static AutomatedMessage createAutoMessage(String title, String body) {
		if (title == null && body == null) {
			return null;
		}
		AutomatedMessage message = new AutomatedMessage();
		message.setTitle(title);
		message.setBody(body);
		return message;
	}
}
