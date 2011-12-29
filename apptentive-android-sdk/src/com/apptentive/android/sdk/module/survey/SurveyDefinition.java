/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class SurveyDefinition {
	private String id;
	private String name;
	private String description;
	private List<QuestionDefinition> questions;

	public SurveyDefinition(JSONObject survey) throws JSONException{
		this.id = survey.getString("id");
		this.name = survey.getString("name");
		this.description = survey.has("description") ? survey.getString("description") : null;
		this.questions = new ArrayList<QuestionDefinition>();
		JSONArray questions = survey.getJSONArray("questions");
		for(int i = 0; i < questions.length(); i++){
			this.questions.add(new QuestionDefinition((JSONObject)questions.get(i)));
		}
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<QuestionDefinition> getQuestions() {
		return questions;
	}
}
