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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class SurveyPayload extends Payload {

	private SurveyDefinition definition;
	private Map<String, String[]> answers;

	public SurveyPayload(SurveyDefinition definition) {
		super();
		this.definition = definition;
		answers = new LinkedHashMap<String, String[]>(definition.getQuestions().size());
		initializeResult();
	}

	public void setAnswer(int questionIndex, String... answers) {
		Question question = definition.getQuestions().get(questionIndex);
		this.answers.put(question.getId(), answers);
	}

	public boolean isAnswered(String questionId) {
		String[] answer = answers.get(questionId);
		return (answer != null && answer.length != 0 && !answer[0].equals(""));
	}

	private void initializeResult() {
		for (Question question : definition.getQuestions()) {
			answers.put(question.getId(), new String[]{""});
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
				String[] array = this.answers.get(key);
				if(array.length == 1 && !array[0].equals("")) {
					answers.put(key, array[0]);
				} else if(array.length > 1){
					JSONArray jsonArray = new JSONArray();
					for (int i = 0; i < array.length; i++) {
						String s = array[i];
						jsonArray.put(array[i]);
					}
					answers.put(key, jsonArray);
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
