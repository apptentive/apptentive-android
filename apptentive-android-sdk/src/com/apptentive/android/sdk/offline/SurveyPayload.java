/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.offline;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.survey.Question;
import com.apptentive.android.sdk.module.survey.SurveyDefinition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public class SurveyPayload extends Payload {

	private SurveyDefinition definition;

	public SurveyPayload(SurveyDefinition definition) {
		super();
		this.definition = definition;
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
			List<Question> questions = this.definition.getQuestions();
			for(Question question : questions) {
				String id = question.getId();
				String[] questionAnswers = question.getAnswers();
				if(questionAnswers.length > 1 || question.getType() == Question.QUESTION_TYPE_MULTICHOICE || question.getType() == Question.QUESTION_TYPE_MULTISELECT) {
					JSONArray jsonArray = new JSONArray();
					for (String answer : questionAnswers) {
						jsonArray.put(answer);
					}
					answers.put(id, jsonArray);
				} else if(questionAnswers.length == 1 && !questionAnswers[0].equals("")) {
					answers.put(id, questionAnswers[0]);
				}
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
