/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.metric;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.model.EventPayload;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import org.json.JSONObject;

import java.util.Map;

import static com.apptentive.android.sdk.ApptentiveHelper.checkConversationQueue;
import static com.apptentive.android.sdk.ApptentiveHelper.dispatchOnConversationQueue;
import static com.apptentive.android.sdk.ApptentiveHelper.isConversationQueue;
import static com.apptentive.android.sdk.ApptentiveLogTag.UTIL;

/**
 * @author Sky Kelsey.
 */
public class MetricModule {

	private static final String KEY_EXCEPTION = "exception";

	public static void sendMetric(EventPayload.EventLabel type) {
		sendMetric(type, null);
	}

	public static void sendMetric(EventPayload.EventLabel type, String trigger) {
		sendMetric(type, trigger, null);
	}

	public static void sendMetric(final EventPayload.EventLabel type, final String trigger, final Map<String, String> data) {
		if (!isConversationQueue()) {
			dispatchOnConversationQueue(new DispatchTask() {
				@Override
				protected void execute() {
					sendMetric(type, trigger, data);
				}
			});
			return;
		}

		Configuration config = Configuration.load();
		if (config.isMetricsEnabled()) {
			ApptentiveLog.v(UTIL, "Sending Metric: %s, trigger: %s, data: %s", type.getLabelName(), trigger, data != null ? data.toString() : "null");
			EventPayload event = new EventPayload(type.getLabelName(), trigger);
			event.putData(data);
			sendEvent(event);
		}
	}

	/**
	 * Used for internal error reporting when we intercept a Throwable that may have otherwise caused a crash.
	 *
	 * @param throwable   An optional throwable that was caught, and which we want to log.
	 * @param description An optional description of what happened.
	 * @param extraData   Any extra data that may have contributed to the Throwable being thrown.
	 */
	public static void sendError(final Throwable throwable, final String description, final String extraData) {
		if (!isConversationQueue()) {
			dispatchOnConversationQueue(new DispatchTask() {
				@Override
				protected void execute() {
					sendError(throwable, description, extraData);
				}
			});
			return;
		}

		EventPayload.EventLabel type = EventPayload.EventLabel.error;
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
				ApptentiveLog.v(UTIL, "Sending Error Metric: %s, data: %s", type.getLabelName(), data.toString());
				EventPayload event = new EventPayload(type.getLabelName(), data);
				sendEvent(event);
			}
		} catch (Exception e) {
			// Since this is the last place in Apptentive code we can catch exceptions, we must catch all other Exceptions to
			// prevent the app from crashing.
			ApptentiveLog.w(UTIL, e, "Error creating Error Metric. Nothing we can do but log this.");
		}
	}

	private static void sendEvent(EventPayload event) {
		checkConversationQueue();

		Conversation conversation = ApptentiveInternal.getInstance().getConversation();
		if (conversation != null) {
			conversation.addPayload(event);
		} else {
			ApptentiveLog.w(UTIL, "Unable to send event '%s': no active conversation", event);
		}
	}
}
