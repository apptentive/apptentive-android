/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import com.apptentive.android.sdk.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class MessageFactory {
	public static ApptentiveMessage fromJson(String json) {
		try {
			JSONObject root = new JSONObject(json);
			ApptentiveMessage.Type type = ApptentiveMessage.Type.valueOf(root.getString(ApptentiveMessage.KEY_TYPE));
			switch (type) {
				case TextMessage:
					// This is ugly, but works.
					ApptentiveMessage apptentiveMessage = new OutgoingTextMessage(json);
					if (!apptentiveMessage.isOutgoingMessage()) {
						apptentiveMessage = new IncomingTextMessage(json);
					}
					return apptentiveMessage;
				case FileMessage:
					return new OutgoingFileMessage(json);
				case AutomatedMessage:
					return new AutomatedMessage(json);
				case unknown:
					break;
				default:
					break;
			}
		} catch (JSONException e) {
			Log.v("Error parsing json as Message: %s", e, json);
		} catch (IllegalArgumentException e) {
			// Unknown unknown #rumsfeld
		}
		return null;
	}
}
