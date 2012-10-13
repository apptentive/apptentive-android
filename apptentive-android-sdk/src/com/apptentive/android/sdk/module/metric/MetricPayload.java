/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.metric;

import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.offline.RecordPayload;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class MetricPayload extends RecordPayload {

	private static final String KEY_RECORD = "record";
	private static final String KEY_DEVICE = "device";
	private static final String KEY_DEVICE_UUID = "uuid";
	private static final String KEY_METRIC = "metric";
	private static final String KEY_METRIC_EVENT = "event";
	private static final String KEY_METRIC_DATA = "data";
	private static final String KEY_METRIC_DATA_TRIGGER = "trigger";


	MetricPayload(String event, String trigger) {
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

	public void putData(Map<String, String> dataToSave) {
		if(dataToSave == null) {
			return;
		}
		try {
			if (has(KEY_RECORD)) {
				JSONObject record = getJSONObject(KEY_RECORD);
				if (record.has(KEY_METRIC)) {
					JSONObject metric = record.getJSONObject(KEY_METRIC);
					if (metric.has(KEY_METRIC_DATA)) {
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
}