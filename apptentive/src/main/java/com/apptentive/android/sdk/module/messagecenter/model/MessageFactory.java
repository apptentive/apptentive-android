/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import android.text.TextUtils;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.ApptentiveMessage;
import com.apptentive.android.sdk.model.CompoundMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageFactory {
	public static ApptentiveMessage fromJson(String json) {
		try {
			// If KEY_TYPE is set to CompoundMessage or not set, treat them as CompoundMessage
			ApptentiveMessage.Type type = ApptentiveMessage.Type.CompoundMessage;
			JSONObject root = new JSONObject(json);
			if (!root.isNull(ApptentiveMessage.KEY_TYPE)) {
				String typeStr = root.getString(ApptentiveMessage.KEY_TYPE);
				if (!TextUtils.isEmpty(typeStr)) {
					type = ApptentiveMessage.Type.valueOf(typeStr);
				}
			}
			switch (type) {
				case CompoundMessage:
					String senderId = null;
					try {
						if (!root.isNull(ApptentiveMessage.KEY_SENDER)) {
							JSONObject sender = root.getJSONObject(ApptentiveMessage.KEY_SENDER);
							if (!sender.isNull((ApptentiveMessage.KEY_SENDER_ID))) {
								senderId = sender.getString(ApptentiveMessage.KEY_SENDER_ID);
							}
						}
					} catch (JSONException e) {
						// Ignore, senderId would be null
					}
					String personId = ApptentiveInternal.getInstance().getPersonId();
					// If senderId is null or same as the locally stored id, construct message as outgoing
					return new CompoundMessage(json, (senderId == null || (personId != null && senderId.equals(personId))));
				case unknown:
					break;
				default:
					break;
			}
		} catch (JSONException e) {
			ApptentiveLog.v("Error parsing json as Message: %s", e, json);
		} catch (IllegalArgumentException e) {
			// Exception treated as unknown type
		}
		return null;
	}
}
