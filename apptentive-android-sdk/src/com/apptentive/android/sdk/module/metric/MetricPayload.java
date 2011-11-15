/*
 * MetricPayload.java
 *
 * Created by Sky Kelsey on 2011-11-14.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.module.metric;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.offline.Payload;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;

import java.util.Date;
import java.util.Map;

public class MetricPayload extends Payload {


	public MetricPayload(Event type) {
		try {
			setString(type.name(), "record", "metric", "event");
			setString(Util.dateToString(new Date()), "record", "date");
		} catch (JSONException e) {
			Log.e("Exception generating metric JSON.", e);
		}
	}

	public void putData(String key, String value) {
		try {
			setString(value, "record", "metric", "data", key);
		} catch (Exception e) {
			Log.e("Unable to add data to metric: " + key + " = " + value);
		}
	}

	public void putAllData(Map<String, String> data) {
		for (String key : data.keySet()) {
			putData(key, data.get(key));
		}
	}

	public static enum Event {
		ratings_provided_rating,
		ratings_postponed_rating,
		ratings_declined_rating,
		feedback_form_triggered,
		app_launch,
		app_exit
	}
}