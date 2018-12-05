/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.ApptentiveMessage;
import com.apptentive.android.sdk.model.CompoundMessage;
import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static com.apptentive.android.sdk.ApptentiveLogTag.MESSAGES;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class MessageFactory {

	public static ApptentiveMessage fromJson(String json) {
		try {
			// If KEY_TYPE is set to CompoundMessage or not set, treat them as CompoundMessage
			ApptentiveMessage.Type type = ApptentiveMessage.Type.CompoundMessage;
			JSONObject root = new JSONObject(json);
			if (!root.isNull(ApptentiveMessage.KEY_TYPE)) {
				String typeStr = root.getString(ApptentiveMessage.KEY_TYPE);
				if (!StringUtils.isNullOrEmpty(typeStr)) {
					type = ApptentiveMessage.Type.valueOf(typeStr);
				}
			}
			switch (type) {
				case CompoundMessage:
					return new CompoundMessage(json);
				case unknown:
					break;
				default:
					break;
			}
		} catch (JSONException e) {
			ApptentiveLog.v(MESSAGES, e, "Error parsing json as Message: %s", json);
			logException(e);
		} catch (IllegalArgumentException e) {
			// Exception treated as unknown type
			logException(e);
		}
		return null;
	}
}
