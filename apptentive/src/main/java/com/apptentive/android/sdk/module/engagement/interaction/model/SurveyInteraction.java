/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import com.apptentive.android.sdk.module.engagement.interaction.model.survey.MultichoiceQuestion;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.MultiselectQuestion;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.Question;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.RangeQuestion;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.SinglelineQuestion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class SurveyInteraction extends Interaction {

	// Configuration
	private static final String KEY_NAME = "name";
	private static final String KEY_DESCRIPTION = "description";
	private static final String KEY_SHOW_SUCCESS_MESSAGE = "show_success_message";
	private static final String KEY_SUCCESS_MESSAGE = "success_message";
	private static final String KEY_REQUIRED = "required";
	private static final String KEY_REQUIRED_TEXT = "required_text";
	private static final String KEY_SUBMIT_TEXT = "submit_text";
	private static final String KEY_VALIDATION_ERROR = "validation_error";
	private static final String KEY_QUESTIONS = "questions";

	public SurveyInteraction(String json) throws JSONException {
		super(json);
	}

	@Override
	public String getTitle() {
		return getName();
	}

	public String getName() {
		return getConfiguration().optString(KEY_NAME, null);
	}

	public String getDescription() {
		return getConfiguration().optString(KEY_DESCRIPTION, null);
	}

	public boolean isShowSuccessMessage() {
		return getConfiguration().optBoolean(KEY_SHOW_SUCCESS_MESSAGE);
	}

	public String getSuccessMessage() {
		return getConfiguration().optString(KEY_SUCCESS_MESSAGE, null);
	}

	public boolean isRequired() {
		return getConfiguration().optBoolean(KEY_REQUIRED);
	}

	public String getRequiredText() {
		return getConfiguration().optString(KEY_REQUIRED_TEXT, null);
	}

	public String getSubmitText() {
		return getConfiguration().optString(KEY_SUBMIT_TEXT, null);
	}

	public String getValidationError() {
		return getConfiguration().optString(KEY_VALIDATION_ERROR, null);
	}

	public List<Question> getQuestions() {
		String requiredText = getRequiredText();
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_QUESTIONS)) {
				List<Question> questions = new ArrayList<Question>();
				JSONArray questionsArray = configuration.getJSONArray(KEY_QUESTIONS);
				for (int i = 0; i < questionsArray.length(); i++) {
					JSONObject questionJson = (JSONObject) questionsArray.get(i);
					Question.Type type = Question.Type.parse(questionJson.getString("type"));
					Question question = null;
					switch (type) {
						case singleline:
							question = new SinglelineQuestion(questionJson.toString());
							break;
						case multichoice:
							question = new MultichoiceQuestion(questionJson.toString());
							break;
						case multiselect:
							question = new MultiselectQuestion(questionJson.toString());
							break;
						case range:
							question = new RangeQuestion(questionJson.toString());
							break;
						case unknown:
						default:
							break;
					}
					if (question != null) {
						question.setRequiredText(requiredText);
						questions.add(question);
					}
				}
				return questions;
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}
}
