/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
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
 * @author Sky Kelsey.
 */
public class MultichoiceQuestion extends BaseQuestion {

	protected int maxSelections;
	protected List<AnswerDefinition> answerChoices = null;

	protected MultichoiceQuestion(JSONObject question) throws JSONException {
		super(question);
		this.maxSelections = 1;
		this.answerChoices = new ArrayList<AnswerDefinition>();
		JSONArray multichoiceChoices = question.getJSONArray("answer_choices");
		for (int i = 0; i < multichoiceChoices.length(); i++) {
			this.answerChoices.add(new AnswerDefinition((JSONObject) multichoiceChoices.get(i)));
		}
	}

	public int getType() {
		return QUESTION_TYPE_MULTICHOICE;
	}

	public int getMaxSelections() {
		return maxSelections;
	}

	public List<AnswerDefinition> getAnswerChoices() {
		return answerChoices;
	}
}
