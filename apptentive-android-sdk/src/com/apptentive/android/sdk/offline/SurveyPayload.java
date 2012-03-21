/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.offline;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.survey.AnswerDefinition;
import com.apptentive.android.sdk.module.survey.QuestionDefinition;
import com.apptentive.android.sdk.module.survey.SurveyDefinition;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class SurveyPayload extends Payload {

	private SurveyDefinition definition;
	private Map<String, String> answers;

	public SurveyPayload(SurveyDefinition definition) {
		super();
		this.definition = definition;
		answers = new LinkedHashMap<String, String>(definition.getQuestions().size());
		initializeResult();
	}

	public void setAnswer(int questionIndex, String answer) {
		QuestionDefinition question = definition.getQuestions().get(questionIndex);
		switch (question.getType()) {
			case singleline:
				answers.put(question.getId(), answer);
				break;
			case multichoice:
				for (AnswerDefinition answerDefinition : question.getAnswerChoices()) {
					if (answerDefinition.getValue().equals(answer)) {
						answers.put(question.getId(), answerDefinition.getId());
						return;
					}
				}
				answers.put(question.getId(), "");
				return;
			default:
				break;
		}
	}

	public String getAnswer(String questionId) {
		return answers.get(questionId);
	}

	private void initializeResult() {
		for (QuestionDefinition question : definition.getQuestions()) {
			switch (question.getType()) {
				case singleline:
					answers.put(question.getId(), "");
					break;
				case multichoice:
					answers.put(question.getId(), "");
					break;
			}
		}
	}

	/**
	 * Return Object with array of Objects that are themselves a single key/value pair.
	 *
	 * @return JSON String
	 */
	@Override
	public String getAsJSON() {
		try {
			JSONObject record = new JSONObject();
			JSONObject survey = new JSONObject();
			survey.put("id", definition.getId());
			JSONObject answers = new JSONObject();
			for (String key : this.answers.keySet()) {
				String value = this.answers.get(key);
				if (value.equals(QuestionDefinition.DEFAULT)) {
					value = "";
				}
				if (value.equals("")) {
					continue;
				}
				answers.put(key, value);
			}
			survey.put("responses", answers);
			record.put("survey", survey);
			root.put("record", record);
			return super.getAsJSON();
		} catch (JSONException e) {
			Log.e("Error encoding survey JSON.", e);
		}
		return null;
	}

}
