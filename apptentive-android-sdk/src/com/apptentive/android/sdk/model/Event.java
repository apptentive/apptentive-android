/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Log;
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
			Log.e("Unable to construct Event.", e);
		}
	}

	public Event(String label, String trigger) {
		this(label, (Map<String, String>) null);
		Map<String, String> data = new HashMap<String, String>();
		data.put(KEY_TRIGGER, trigger);
		putData(data);
	}

	@Override
	protected void initBaseType() {
		setBaseType(BaseType.event);
	}

	public void putData(Map<String, String> data) {
		if (data == null || data.isEmpty()) {
			return;
		}
		try {
			JSONObject dataObject;
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
			Log.e("Unable to add data to Event.", e);
		}
	}

	public static enum EventLabel {

		app__launch("app.launch"),
		app__exit("app.exit"),

		enjoyment_dialog__launch("enjoyment_dialog.launch"),
		enjoyment_dialog__yes("enjoyment_dialog.yes"),
		enjoyment_dialog__no("enjoyment_dialog.no"),

		rating_dialog__launch("rating_dialog.launch"),
		rating_dialog__rate("rating_dialog.rate"),
		rating_dialog__remind("rating_dialog.remind"),
		rating_dialog__decline("rating_dialog.decline"),

		survey__launch("survey.launch"),
		survey__cancel("survey.cancel"),
		survey__submit("survey.submit"),
		survey__question_response("survey.question_response"),

		message_center__launch("message_center.launch"),
		message_center__close("message_center.close"),
		message_center__attach("message_center.attach"),
		message_center__read("message_center.read"),

		error("error");

		private final String labelName;

		private EventLabel(String labelName) {
			this.labelName = labelName;
		}

		public String getLabelName() {
			return labelName;
		}
	}
}