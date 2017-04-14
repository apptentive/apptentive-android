/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveLog;

import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class LogoutPayloadFactory {
	public static LogoutPayload fromJson(String json) {
		try {
			return new LogoutPayload(json);
		} catch (JSONException e) {
			ApptentiveLog.v("Error parsing json as LogoutPayload: %s", e, json);
		} catch (IllegalArgumentException e) {
			// Unknown unknown #rumsfeld
		}
		return null;
	}
}
