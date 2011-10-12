/*
 * Survey.java
 *
 * Created by Sky Kelsey on 2011-10-09.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.offline;

import com.apptentive.android.sdk.ALog;
import com.apptentive.android.sdk.survey.AnswerDefinition;
import com.apptentive.android.sdk.survey.QuestionDefinition;
import com.apptentive.android.sdk.survey.SurveyDefinition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;


public class Survey extends JSONPayload{

	private final ALog log = new ALog(this.getClass());

	private SurveyDefinition definition;
	private Map<String, String> answers;

	public Survey(SurveyDefinition definition) {
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

	public String getAnswer(int questionIndex){
		QuestionDefinition question = definition.getQuestions().get(questionIndex);
		return answers.get(question.getId());
	}

	private void initializeResult(){
		for(QuestionDefinition question : definition.getQuestions()){
			switch(question.getType()){
				case singleline:
					answers.put(question.getId(), "");
					break;
				case multichoice:
					answers.put(question.getId(), question.getAnswerChoices().get(0).getId());
					break;
			}
		}
	}

	/**
	 * Return Object with array of Objects that are themselves a single key/value pair.
	 * <pre>
	 * Example:
	 * {
	 *   "record":{
	 *     "survey":{
	 *       "id":"4e909ab2a902915fbc000001",
	 *       "questions":[
	 *         {
	 *           "4e909bc4a902915fbc000011":"4e909be0a902915fbc000005"
	 *         },
	 *         {
	 *           "4e909c39a902915fbc000002":"two"
	 *         }
	 *       ]
	 *     }
	 *   }
	 * }
	 * </pre>
	 * @return JSON String
	 */
	@Override
	public String getAsJSON() {
		try{
			JSONObject root = new JSONObject();
			JSONObject record = new JSONObject();
			JSONObject survey = new JSONObject();
			survey.put("id", definition.getId());
			JSONArray answers = new JSONArray();
			for(String key : this.answers.keySet()){

				String value = this.answers.get(key);
				JSONObject answer = new JSONObject();
				answer.put("question_id", key);
				answer.put("value", value);
				answers.put(answer);
			}
			survey.put("responses", answers);
			record.put("survey", survey);
			root.put("record", record);
			return root.toString();
		}catch(JSONException e){
			log.e("Error encoding survey JSON.", e);
		}
		return null;
	}

	/*
{
    "record": {
        "survey": {
            "id": "4e90b16ccb7ca90006000001",
            "responses": [
                {
                    "value": "4e90b16ccb7ca90006000005",
                    "question_id": "4e90b16ccb7ca90006000002"
                },
                {
                    "value": "good",
                    "question_id": "4e90b16ccb7ca90006000006"
                }
            ]
        }
    }
}{
    "record": {
        "survey": {
            "id": "4e909ab2a902915fbc000001",
            "responses": [
                {
                    "question_id": "4e909bc4a902915fbc000003",
                    "value": "4e909be6a902915fbc000006"
                },
                {
                    "question_id": "4e909c39a902915fbc000007",
                    "value": "two"
                }
            ]
        }
    }
}
{
	"record":{
		"survey":{
			"id":"4e909ab2a902915fbc000001",
			"questions":[
				{
					"4e909bc4a902915fbc000011":"4e909be0a902915fbc000005"
				},
				{
					"4e909c39a902915fbc000002":"two"
				}
			]
		}
	}
}
	 */
}
