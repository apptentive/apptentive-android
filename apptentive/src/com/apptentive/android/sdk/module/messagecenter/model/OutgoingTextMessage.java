/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import com.apptentive.android.sdk.Log;

import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class OutgoingTextMessage extends ApptentiveMessage implements MessageCenterUtil.OutgoingItem {

	private static final String KEY_BODY = "body";

	private boolean isLast;

	public OutgoingTextMessage() {
		super();
	}

	public OutgoingTextMessage(String json) throws JSONException {
		super(json);
	}

	@Override
	protected void initType() {
		setType(Type.TextMessage);
	}

	public String getBody() {
		try {
			if (!isNull(KEY_BODY)) {
				return getString(KEY_BODY);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public void setBody(String body) {
		try {
			put(KEY_BODY, body);
		} catch (JSONException e) {
			Log.e("Unable to set message body.");
		}
	}

	@Override
	public boolean isLastSent() {
		return isLast;
	}

	@Override
	public void setLastSent(boolean bVal) {
		isLast = bVal;
	}
}
