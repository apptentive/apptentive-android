/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.metric;

import android.content.Context;
import android.content.SharedPreferences;
import com.apptentive.android.sdk.offline.PayloadManager;

/**
 * @author Sky Kelsey.
 */
public class MetricModule {

	private static Context appContext = null;

	public static void setContext(Context appContext) {
		MetricModule.appContext = appContext;
	}

	public static void sendMetric(MetricModule.Event event) {
		sendMetric(event, null);
	}

	public static void sendMetric(MetricModule.Event event, String trigger) {
		SharedPreferences prefs = appContext.getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE);
		if (prefs.getBoolean("appConfiguration.metrics_enabled", true)) {
			PayloadManager.getInstance().putPayload(new MetricPayload(event.getRecordName(), trigger));
		}
	}

	public static enum Event {
		enjoyment_dialog__launch("enjoyment_dialog.launch"),
		enjoyment_dialog__yes("enjoyment_dialog.yes"),
		enjoyment_dialog__no("enjoyment_dialog.no"),
		rating_dialog__launch("rating_dialog.launch"),
		rating_dialog__rate("rating_dialog.rate"),
		rating_dialog__remind("rating_dialog.remind"),
		rating_dialog__decline("rating_dialog.decline"),
		feedback_dialog__launch("feedback_dialog.launch"),
		feedback_dialog__cancel("feedback_dialog.cancel"),
		app__launch("app.launch"),
		app__exit("app.exit");

		private final String recordName;

		Event(String recordName) {
			this.recordName = recordName;
		}

		public String getRecordName() {
			return recordName;
		}
	}
}
