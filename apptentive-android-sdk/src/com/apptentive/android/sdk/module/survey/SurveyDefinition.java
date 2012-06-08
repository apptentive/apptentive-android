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

	/**
	 * If true, this survey must not be skipped.
	 */
	private boolean required;

	private String successMessage;

	private boolean showSuccessMessage;


	private List<QuestionDefinition> questions;


	public SurveyDefinition(JSONObject survey) throws JSONException{
		this.id = survey.getString("id");
		this.name = survey.getString("name");
		this.description = survey.has("description") ? survey.getString("description") : null;
		this.required = survey.has("required") ? survey.optBoolean("required") : null;
		this.successMessage = survey.has("success_message") ? survey.getString("success_message") : null;
		this.showSuccessMessage = survey.has("show_success_message") ? survey.optBoolean("show_success_message") : null;
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

	public boolean isRequired() {
		return required;
	}

	public String getSuccessMessage() {
		if(successMessage != null && successMessage.equals("")) {
			return null;
		}
		return successMessage;
	}

	public boolean isShowSuccessMessage() {
		return showSuccessMessage;
	}

	public List<QuestionDefinition> getQuestions() {
		return questions;
	}
}
