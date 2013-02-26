/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.metric;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.ConversationItem;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class Event extends ConversationItem {


	private static final String KEY_LABEL = "label";
	private static final String KEY_DATA = "data";
	private static final String KEY_TRIGGER = "trigger";


	public Event(String json) throws JSONException {
		super(json);
	}

	public Event(String label, Map<String, String> data) {
		super();
		try {
			put(KEY_LABEL, label);
			if (data != null && !data.isEmpty()) {
				JSONObject dataObject = new JSONObject();
				for (String key : data.keySet()) {
					dataObject.put(key, data.get(key));
				}
				put(KEY_DATA, dataObject);
			}
		} catch (JSONException e) {
			Log.e("Unable to construct MetricPayload.", e);
		}
	}

	public Event(String label, String trigger) {
		this(label, (Map<String, String>) null);
		Map<String, String> data = new HashMap<String, String>();
		data.put(KEY_TRIGGER, trigger);
		putData(data);
	}

	@Override
	protected void initType() {
		setType(Type.Event);
	}

	public void putData(Map<String, String> data) {
		if (data == null || data.isEmpty()) {
			return;
		}
		try {
			JSONObject dataObject = null;
			if (isNull(KEY_DATA)) {
				dataObject = new JSONObject();
				put(KEY_DATA, dataObject);
			} else {
				dataObject = getJSONObject(KEY_DATA);
			}
			for (String key : data.keySet()) {
				dataObject.put(key, data.get(key));
			}
		} catch (JSONException e) {
			Log.e("Unable to add data to metric.", e);
		}
	}

	public static enum EventLabel {
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

		private final String labelName;

		private EventLabel(String labelName) {
			this.labelName = labelName;
		}

		public String getLabelName() {
			return labelName;
		}
	}

}