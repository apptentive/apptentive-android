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


	public MetricPayload(Event type) {
		try {
			setString(type.getRecordName(), "record", "metric", "event");
			setString(GlobalInfo.androidId, "record", "device", "uuid");
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