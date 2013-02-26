/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.metric;

import android.content.Context;
import android.content.SharedPreferences;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.EventManager;
import com.apptentive.android.sdk.util.Constants;

import java.util.Map;

/**
 * TODO: Get rid of all of this.
 * @author Sky Kelsey.
 */
public class MetricModule {

	private static Context appContext = null;

	public static void setContext(Context appContext) {
		MetricModule.appContext = appContext;
	}

	public static void sendMetric(Event.EventLabel type) {
		sendMetric(type, null);
	}

	public static void sendMetric(Event.EventLabel type, String trigger) {
		sendMetric(type, trigger, null);
	}

	public static void sendMetric(Event.EventLabel type, String trigger, Map<String, String> data) {
		Log.v("Sending Metric: %s, trigger: %s, data: %s", type.getLabelName(), trigger, data != null ? data.toString() : "null");

		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		if (prefs.getBoolean(Constants.PREF_KEY_APP_METRICS_ENABLED, true)) {
			Event event = new Event(type.getLabelName(), trigger);
			event.putData(data);
			EventManager.sendEvent(event);
		}
	}
}
