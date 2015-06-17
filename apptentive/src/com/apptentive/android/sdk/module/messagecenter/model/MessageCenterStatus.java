/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class MessageCenterStatus extends JSONObject implements MessageCenterListItem {
	public static final int STATUS_CONFIRMATION = 0;
	public static final int STATUS_NO_CONNECTION = 1;
	public static final int STATUS_REJECTED = 2;

	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";

	private int type;
    private String title;
	private String body;

	public MessageCenterStatus(int type, String tile, String body) {
		this.type = type;
		this.title = tile;
		this.body = body;
	}
	public String getTitle() {
		try {
			if (!isNull((KEY_TITLE))) {
				return getString(KEY_TITLE);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return title;
	}

	public String getBody() {
		try {
			if (!isNull((KEY_BODY))) {
				return getString(KEY_BODY);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return body;
	}

	public int getType() {
		return type;
	}

}
