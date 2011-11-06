/*
 * QuestionDefinition.java
 *
 * Created by Sky Kelsey on 2011-10-08.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.module.survey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QuestionDefinition {

	public static final String DEFAULT = "Choose one...";

	private String id;
	private Type type;
	private String value;
	private List<AnswerDefinition> answerChoices = null;

	public QuestionDefinition(JSONObject question) throws JSONException {
		this.id = question.getString("id");
		this.type = Type.valueOf(question.getString("type"));
		this.value = question.getString("value");
		switch(this.type){
			case singleline:
				break;
			case multichoice:
				this.answerChoices = new ArrayList<AnswerDefinition>();
				JSONArray answerChoices = question.getJSONArray("answer_choices");
				for(int i = 0; i < answerChoices.length(); i++){
					this.answerChoices.add(new AnswerDefinition((JSONObject)answerChoices.get(i)));
				}
				break;
			default:
				break;
		}
	}

	public enum Type{
		multichoice,
		singleline
	}

	public String getId() {
		return id;
	}

	public Type getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public List<AnswerDefinition> getAnswerChoices() {
		return answerChoices;
	}
}
