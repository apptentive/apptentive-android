/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.interaction.model.SurveyInteraction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class SurveyResponsePayload extends ConversationItem {

	private static final String KEY_SURVEY_ID = "id";

	private static final String KEY_SURVEY_ANSWERS = "answers";

	public SurveyResponsePayload(String json) throws JSONException {
		super(json);
	}

	public SurveyResponsePayload(SurveyInteraction definition, Map<String, Object> answers) {
		super();

		try {
			put(KEY_SURVEY_ID, definition.getId());
			JSONObject answersJson = new JSONObject();
			for (String key : answers.keySet()) {
				answersJson.put(key, answers.get(key));
			}

			put(KEY_SURVEY_ANSWERS, answersJson);
		} catch (JSONException e) {
			ApptentiveLog.e("Unable to construct survey payload.", e);
		}
	}

	public String getId() {
		return optString(KEY_SURVEY_ID, "");
	}

	@Override
	protected void initBaseType() {
		setBaseType(BaseType.survey);
	}
}
