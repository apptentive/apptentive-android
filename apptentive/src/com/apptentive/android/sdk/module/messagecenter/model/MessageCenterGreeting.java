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
public class MessageCenterGreeting extends JSONObject implements MessageCenterListItem {
	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";

	public String getTitle() {
		try {
			if (!isNull((KEY_TITLE))) {
				return getString(KEY_TITLE);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return "Hello.";
	}

	public String getBody() {
		try {
			if (!isNull((KEY_BODY))) {
				return getString(KEY_BODY);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return "I'm Samantha. I work at Apptentive. Please tell me what we can do to improve the app.";
	}


}
