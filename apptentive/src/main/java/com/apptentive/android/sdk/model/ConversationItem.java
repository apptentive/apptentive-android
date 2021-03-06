/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;

public abstract class ConversationItem extends JsonPayload {

	protected static final String KEY_CLIENT_CREATED_AT = "client_created_at";
	protected static final String KEY_CLIENT_CREATED_AT_UTC_OFFSET = "client_created_at_utc_offset";

	protected ConversationItem(PayloadType type) {
		super(type);

		double seconds = Util.currentTimeSeconds();
		int utcOffset = Util.getUtcOffset();

		setClientCreatedAt(seconds);
		setClientCreatedAtUtcOffset(utcOffset);
	}

	protected ConversationItem(PayloadType type, String json) throws JSONException {
		super(type, json);
	}

	public Double getClientCreatedAt() {
		return getDouble(KEY_CLIENT_CREATED_AT);
	}

	public void setClientCreatedAt(double clientCreatedAt) {
		put(KEY_CLIENT_CREATED_AT, clientCreatedAt);
	}

	/**
	 * This is made public primarily so that unit tests can be made to run successfully no matter what the time zone.
	 */
	public void setClientCreatedAtUtcOffset(int clientCreatedAtUtcOffset) {
		put(KEY_CLIENT_CREATED_AT_UTC_OFFSET, clientCreatedAtUtcOffset);
	}
}
