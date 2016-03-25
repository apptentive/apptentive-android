/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.metric;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.model.EventManager;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONObject;

import java.util.Map;

/**
 * @author Sky Kelsey.
 */
public class MetricModule {

	private static final String KEY_EXCEPTION = "exception";

	public static void sendMetric(Event.EventLabel type) {
		sendMetric(type, null);
	}

	public static void sendMetric(Event.EventLabel type, String trigger) {
		sendMetric(type, trigger, null);
	}

	public static void sendMetric(Event.EventLabel type, String trigger, Map<String, String> data) {
		Configuration config = Configuration.load();
		if (config.isMetricsEnabled()) {
			ApptentiveLog.v("Sending Metric: %s, trigger: %s, data: %s", type.getLabelName(), trigger, data != null ? data.toString() : "null");
			Event event = new Event(type.getLabelName(), trigger);
			event.putData(data);
			EventManager.sendEvent(event);
		}
	}

	/**
	 * Used for internal error reporting when we intercept a Throwable that may have otherwise caused a crash.
	 *
	 * @param throwable   An optional throwable that was caught, and which we want to log.
	 * @param description An optional description of what happened.
	 * @param extraData   Any extra data that may have contributed to the Throwable being thrown.
	 */
	public static void sendError(Throwable throwable, String description, String extraData) {
		Event.EventLabel type = Event.EventLabel.error;
		try {
			JSONObject data = new JSONObject();
			data.put("thread", Thread.currentThread().getName());
			if (throwable != null) {
				JSONObject exception = new JSONObject();
				exception.put("message", throwable.getMessage());
				exception.put("stackTrace", Util.stackTraceAsString(throwable));
				data.put(KEY_EXCEPTION, exception);
			}
			if (description != null) {
				data.put("description", description);
			}
			if (extraData != null) {
				data.put("extraData", extraData);
			}
			Configuration config = Configuration.load();
			if (config.isMetricsEnabled()) {
				ApptentiveLog.v("Sending Error Metric: %s, data: %s", type.getLabelName(), data.toString());
				Event event = new Event(type.getLabelName(), data);
				EventManager.sendEvent(event);
			}
		} catch (Exception e) {
			// Since this is the last place in Apptentive code we can catch exceptions, we must catch all other Exceptions to
			// prevent the app from crashing.
			ApptentiveLog.w("Error creating Error Metric. Nothing we can do but log this.", e);
		}
	}
}
