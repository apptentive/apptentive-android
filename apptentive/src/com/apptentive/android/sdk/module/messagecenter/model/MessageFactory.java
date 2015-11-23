/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import android.content.Context;
import android.text.TextUtils;

import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class MessageFactory {
	public static ApptentiveMessage fromJson(Context appContext, String json) {
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
						// Ignore, snederId would be null
					}
					String storedId = GlobalInfo.getPersonId(appContext);
					// If senderId is null or same as the locally stored id, construct message as outgoing
					return new CompoundMessage(json, (senderId == null || senderId.equals(storedId)));
				case unknown:
					break;
				default:
					break;
			}
		} catch (JSONException e) {
			Log.v("Error parsing json as Message: %s", e, json);
		} catch (IllegalArgumentException e) {
			// Exception treated as unknown type
		}
		return null;
	}
}
