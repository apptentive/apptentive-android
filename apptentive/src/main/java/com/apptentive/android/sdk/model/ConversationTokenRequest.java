/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveLogTag;
import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class ConversationTokenRequest extends JSONObject {
	public ConversationTokenRequest() {
	}

	public void setDevice(DevicePayload device) {
		try {
			put(DevicePayload.KEY, device == null ? null : device.getJsonObject());
		} catch (JSONException e) {
			ApptentiveLog.e(CONVERSATION, "Error adding %s to ConversationTokenRequest", DevicePayload.KEY);
			logException(e);
		}
	}

	public void setSdk(SdkPayload sdk) {
		try {
			put(SdkPayload.KEY, sdk == null ? null : sdk.getJsonObject());
		} catch (JSONException e) {
			ApptentiveLog.e(CONVERSATION, "Error adding %s to ConversationTokenRequest", SdkPayload.KEY);
			logException(e);
		}
	}

	public void setPerson(PersonPayload person) {
		try {
			put(PersonPayload.KEY, person == null ? null : person.getJsonObject());
		} catch (JSONException e) {
			ApptentiveLog.e(CONVERSATION, "Error adding %s to ConversationTokenRequest", PersonPayload.KEY);
			logException(e);
		}
	}

	public void setSdkAndAppRelease(SdkPayload sdkPayload, AppReleasePayload appReleasePayload) {
		JSONObject combinedJson = new JSONObject();

		if (sdkPayload != null) {
			Iterator<String> keys = sdkPayload.getJsonObject().keys();
			while (keys.hasNext()) {
				String key = keys.next();
				try {
					combinedJson.put("sdk_" + key, sdkPayload.getJsonObject().opt(key));
				} catch (JSONException e) {
					logException(e);
				}
			}

		}
		if (appReleasePayload != null) {
			Iterator<String> keys = appReleasePayload.getJsonObject().keys();
			while (keys.hasNext()) {
				String key = keys.next();
				try {
					combinedJson.put(key, appReleasePayload.getJsonObject().opt(key));
				} catch (JSONException e) {
					logException(e);
				}
			}
		}

		try {
			put("app_release", combinedJson);
		} catch (JSONException e) {
			logException(e);
		}
	}
}
