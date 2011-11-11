/*
 * Survey.java
 *
 * Created by Sky Kelsey on 2011-10-09.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.offline;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.survey.AnswerDefinition;
import com.apptentive.android.sdk.module.survey.QuestionDefinition;
import com.apptentive.android.sdk.module.survey.SurveyDefinition;
import com.apptentive.android.sdk.offline.Payload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;


public class SurveyPayload extends Payload {

	private SurveyDefinition definition;
	private Map<String, String> answers;

	public SurveyPayload(SurveyDefinition definition) {
		super();
		this.definition = definition;
		answers = new LinkedHashMap<String, String>(definition.getQuestions().size());
		initializeResult();
	}

	public void setAnswer(int questionIndex, String answer){
		QuestionDefinition question = definition.getQuestions().get(questionIndex);
		switch(question.getType()){
			case singleline:
				answers.put(question.getId(), answer);
				break;
			case multichoice:
				for(AnswerDefinition answerDefinition : question.getAnswerChoices()){
					if(answerDefinition.getValue().equals(answer)){
						answers.put(question.getId(), answerDefinition.getId());
						return;
					}
				}
				break;
			default:
				break;
		}
	}

	/**
	 * For multi choice, 
	 * @return
	 */
	public boolean hasBeenAnswered(){
		for(String key : answers.keySet()){
			if(!answers.get(key).equals("")){
				return true;
			}
		}
		return false;
	}

	private void initializeResult(){
		for(QuestionDefinition question : definition.getQuestions()){
			switch(question.getType()){
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
	 * @return JSON String
	 */
	@Override
	public String getAsJSON() {
		try{
			JSONObject record = new JSONObject();
			JSONObject survey = new JSONObject();
			survey.put("id", definition.getId());
			JSONArray answers = new JSONArray();
			for(String key : this.answers.keySet()){
				String value = this.answers.get(key);
				if(value.equals(QuestionDefinition.DEFAULT)){
					value = "";
				}
				if(value.equals("")){
					continue;
				}
				JSONObject answer = new JSONObject();
				answer.put("question_id", key);
				answer.put("value", value);
				answers.put(answer);
			}
			survey.put("responses", answers);
			record.put("survey", survey);
			root.put("record", record);
			return super.getAsJSON();
		}catch(JSONException e){
			Log.e("Error encoding survey JSON.", e);
		}
		return null;
	}

}
