/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.offline;

import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.ConversationItem;
import com.apptentive.android.sdk.module.survey.Question;
import com.apptentive.android.sdk.module.survey.SurveyDefinition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public class SurveyPayload extends ConversationItem {

	private static final String KEY_RECORD = "record";
	private static final String KEY_DEVICE = "device";
	private static final String KEY_DEVICE_UUID = "uuid";

	private static final String KEY_SURVEY = "survey";
	private static final String KEY_SURVEY_ID = "id";

	private static final String KEY_SURVEY_RESPONSES = "responses";

	public SurveyPayload(String json) throws JSONException {
		super(json);
	}

	public SurveyPayload(SurveyDefinition definition) {
		super();

		try {
			JSONObject record = new JSONObject();
			put(KEY_RECORD, record);

			JSONObject device = new JSONObject();
			record.put(KEY_DEVICE, device);
			device.put(KEY_DEVICE_UUID, GlobalInfo.androidId);

			JSONObject survey = new JSONObject();
			record.put(KEY_SURVEY, survey);
			survey.put(KEY_SURVEY_ID, definition.getId());

			JSONObject responses = new JSONObject();
			survey.put(KEY_SURVEY_RESPONSES, responses);

			List<Question> questions = definition.getQuestions();
			for(Question question : questions) {
				String id = question.getId();
				String[] questionAnswers = question.getAnswers();
				if(questionAnswers.length > 1 || question.getType() == Question.QUESTION_TYPE_MULTISELECT) {
					JSONArray jsonArray = new JSONArray();
					for (String answer : questionAnswers) {
						jsonArray.put(answer);
					}
					responses.put(id, jsonArray);
				} else if(questionAnswers.length == 1 && !questionAnswers[0].equals("")) {
					responses.put(id, questionAnswers[0]);
				}
			}
		} catch (JSONException e) {
			Log.e("Unable to construct survey payload.", e);
		}
	}

	@Override
	protected void initType() {
		setType(Type.survey);
	}

	@Override
	public String marshallForSending() {
		return toString();
	}
}
