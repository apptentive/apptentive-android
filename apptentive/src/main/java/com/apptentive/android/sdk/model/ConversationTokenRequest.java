/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveLog;

import org.json.JSONException;
import org.json.JSONObject;

public class ConversationTokenRequest extends JSONObject {


	public ConversationTokenRequest() {
	}

	public void setDevice(DevicePayload device) {
		try {
			put(DevicePayload.KEY, device);
		} catch (JSONException e) {
			ApptentiveLog.e("Error adding %s to ConversationTokenRequest", DevicePayload.KEY);
		}
	}

	public void setSdk(Sdk sdk) {
		try {
			put(Sdk.KEY, sdk);
		} catch (JSONException e) {
			ApptentiveLog.e("Error adding %s to ConversationTokenRequest", Sdk.KEY);
		}
	}

	public void setPerson(Person person) {
		try {
			put(Person.KEY, person);
		} catch (JSONException e) {
			ApptentiveLog.e("Error adding %s to ConversationTokenRequest", Person.KEY);
		}
	}

	public void setAppRelease(AppRelease appRelease) {
		try {
			put(appRelease.getBaseType().name(), appRelease);
		} catch (JSONException e) {
			ApptentiveLog.e("Error adding %s to ConversationTokenRequest", appRelease.getBaseType().name());
		}
	}
}
