/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class ActivityFeedTokenRequest extends JSONObject {


	public ActivityFeedTokenRequest() {
	}

	public void setDevice(Device device) {
		try {
			put(Device.KEY, device);
		} catch (JSONException e) {
			Log.e("Error adding %s to ActivityFeedRequest", Device.KEY);
		}
	}

	public void setPerson(Person person) {
		try {
			put(Person.KEY, person);
		} catch (JSONException e) {
			Log.e("Error adding %s to ActivityFeedRequest", Person.KEY);
		}
	}

	//TODO: Handle client info as well.
}
