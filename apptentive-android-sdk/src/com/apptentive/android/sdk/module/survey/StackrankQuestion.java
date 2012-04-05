/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Sky Kelsey.
 */
public class StackrankQuestion extends BaseQuestion {

	protected List<AnswerDefinition> answerChoices;

	protected StackrankQuestion(JSONObject question) throws JSONException {
		super(question);
		this.answerChoices = new LinkedList<AnswerDefinition>();
		JSONArray stackrankChoices = question.getJSONArray("answer_choices");
		for (int i = 0; i < stackrankChoices.length(); i++) {
			this.answerChoices.add(new AnswerDefinition((JSONObject) stackrankChoices.get(i)));
		}
	}

	public int getType() {
		return QUESTION_TYPE_STACKRANK;
	}

	public List<AnswerDefinition> getAnswerChoices() {
		return answerChoices;
	}
}
