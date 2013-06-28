/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.metric;

import android.content.Context;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.model.EventManager;

import java.util.Map;

/**
 * @author Sky Kelsey.
 */
public class MetricModule {

	public static void sendMetric(Context context, Event.EventLabel type) {
		sendMetric(context, type, null);
	}

	public static void sendMetric(Context context, Event.EventLabel type, String trigger) {
		sendMetric(context, type, trigger, null);
	}

	public static void sendMetric(Context context, Event.EventLabel type, String trigger, Map<String, String> data) {
		Configuration config = Configuration.load(context);
		if (config.isMetricsEnabled()) {
			Log.v("Sending Metric: %s, trigger: %s, data: %s", type.getLabelName(), trigger, data != null ? data.toString() : "null");
			Event event = new Event(type.getLabelName(), trigger);
			event.putData(data);
			EventManager.sendEvent(context, event);
		}
	}
}
