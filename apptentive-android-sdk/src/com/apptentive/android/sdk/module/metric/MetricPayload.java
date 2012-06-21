/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.metric;

import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.offline.Payload;
import org.json.JSONException;

import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class MetricPayload extends Payload {


	MetricPayload(String event, String trigger) {
		try {
			setString(event, "record", "metric", "event");
			setString(GlobalInfo.androidId, "record", "device", "uuid");
			if(trigger != null) {
				putData("trigger", trigger);
			}
		} catch (JSONException e) {
			Log.e("Exception generating metric JSON.", e);
		}
	}

	void putData(String key, String value) {
		try {
			setString(value, "record", "metric", "data", key);
		} catch (Exception e) {
			Log.e("Unable to add data to metric: " + key + " = " + value);
		}
	}

	void putAllData(Map<String, String> data) {
		if(data != null) {
			for (String key : data.keySet()) {
				putData(key, data.get(key));
			}
		}
	}
}