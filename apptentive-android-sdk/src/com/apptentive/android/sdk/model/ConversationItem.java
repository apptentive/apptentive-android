/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Log;
import org.json.JSONException;

import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

/**
 * @author Sky Kelsey
 */
public abstract class ConversationItem extends Payload {

	protected static final String KEY_NONCE = "nonce";
	protected static final String KEY_CLIENT_CREATED_AT = "client_created_at";
	protected static final String KEY_CLIENT_CREATED_AT_TIMEZONE = "client_created_at_timezone";
	protected static final String KEY_CLIENT_CREATED_AT_UTC_OFFSET = "client_created_at_utc_offset";

	protected ConversationItem() {
		super();
		setNonce(UUID.randomUUID().toString());

		long millis = new Date().getTime();
		double point = (double)millis;
		double seconds = point / 1000;
		TimeZone timezone = TimeZone.getDefault();
		Date now = new Date();
		int utcOffset = timezone.getOffset(now.getTime()) / 1000;

		setClientCreatedAt(seconds);
		setClientCreatedAtTimezone(timezone.getID());
		setClientCreatedAtUtcOffset(utcOffset);

	}

	protected ConversationItem(String json) throws JSONException {
		super(json);
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

	private void setClientCreatedAtTimezone(String clientCreatedAtTimezone) {
		try {
			put(KEY_CLIENT_CREATED_AT_TIMEZONE, clientCreatedAtTimezone);
		} catch (JSONException e) {
			Log.e("Exception setting ConversationItem's %s field.", e, KEY_CLIENT_CREATED_AT_TIMEZONE);
		}
	}

	private void setClientCreatedAtUtcOffset(int clientCreatedAtUtcOffset) {
		try {
			put(KEY_CLIENT_CREATED_AT_UTC_OFFSET, clientCreatedAtUtcOffset);
		} catch (JSONException e) {
			Log.e("Exception setting ConversationItem's %s field.", e, KEY_CLIENT_CREATED_AT_UTC_OFFSET);
		}
	}


}
