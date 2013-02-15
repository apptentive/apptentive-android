/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.metric;

import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.ActivityFeedItem;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class Event extends ActivityFeedItem {

	private static final String KEY_RECORD = "record";
	private static final String KEY_DEVICE = "device";
	private static final String KEY_DEVICE_UUID = "uuid";
	private static final String KEY_METRIC = "metric";
	private static final String KEY_METRIC_EVENT = "event";
	private static final String KEY_METRIC_DATA = "data";
	private static final String KEY_METRIC_DATA_TRIGGER = "trigger";



	public Event(String json) throws JSONException {
		super(json);
	}

	public Event(String event, String trigger) {
		super();

		try {
			JSONObject record = new JSONObject();
			put(KEY_RECORD, record);
			JSONObject device = new JSONObject();
			record.put(KEY_DEVICE, device);
			device.put(KEY_DEVICE_UUID, GlobalInfo.androidId);
			JSONObject metric = new JSONObject();
			record.put(KEY_METRIC, metric);
			metric.put(KEY_METRIC_EVENT, event);
			JSONObject data = new JSONObject();
			metric.put(KEY_METRIC_DATA, data);
			data.put(KEY_METRIC_DATA_TRIGGER, trigger);
		} catch (JSONException e) {
			Log.e("Unable to construct MetricPayload.", e);
		}
	}

	@Override
	protected void initType() {
		setType(Type.Event);
	}

	public void putData(Map<String, String> dataToSave) {
		if(dataToSave == null) {
			return;
		}
		try {
			if (!isNull(KEY_RECORD)) {
				JSONObject record = getJSONObject(KEY_RECORD);
				if (!record.isNull(KEY_METRIC)) {
					JSONObject metric = record.getJSONObject(KEY_METRIC);
					if (!metric.isNull(KEY_METRIC_DATA)) {
						JSONObject data = metric.getJSONObject(KEY_METRIC_DATA);
						for (String key : dataToSave.keySet()) {
							if (dataToSave.get(key) != null) {
								data.put(key, dataToSave.get(key));
							}
						}
					}
				}
			}
		} catch (JSONException e) {
			Log.e("Unable to add data to metric.", e);
		}
	}

	public static enum EventType {
		enjoyment_dialog__launch("enjoyment_dialog.launch"),
		enjoyment_dialog__yes("enjoyment_dialog.yes"),
		enjoyment_dialog__no("enjoyment_dialog.no"),
		rating_dialog__launch("rating_dialog.launch"),
		rating_dialog__rate("rating_dialog.rate"),
		rating_dialog__remind("rating_dialog.remind"),
		rating_dialog__decline("rating_dialog.decline"),
		feedback_dialog__launch("feedback_dialog.launch"),
		feedback_dialog__submit("feedback_dialog.submit"),
		feedback_dialog__cancel("feedback_dialog.cancel"),
		survey__launch("survey.launch"),
		survey__cancel("survey.cancel"),
		survey__submit("survey.submit"),
		survey__question_response("survey.question_response"),
		app__launch("app.launch"),
		app__exit("app.exit"),
		app__session_start("app.session_start"),
		app__session_end("app.session_end");

		private final String recordName;

		EventType(String recordName) {
			this.recordName = recordName;
		}

		public String getRecordName() {
			return recordName;
		}
	}

}