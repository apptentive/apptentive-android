/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
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
 * TODO: This needs to change to match the other question types.
 *
 * @author Sky Kelsey.
 */
public class StackrankQuestion extends BaseQuestion {

	private static final String KEY_ANSWER_CHOICES = "answer_choices";

	protected StackrankQuestion(String json) throws JSONException {
		super(json);
	}

	public int getType() {
		return QUESTION_TYPE_STACKRANK;
	}

	public List<AnswerDefinition> getAnswerChoices() {
		try {
			List<AnswerDefinition> answerChoices = new ArrayList<AnswerDefinition>();
			JSONArray stackrankChoices = optJSONArray(KEY_ANSWER_CHOICES);
			if (stackrankChoices != null) {
				for (int i = 0; i < stackrankChoices.length(); i++) {
					JSONObject answer = stackrankChoices.optJSONObject(i);
					if (answer != null) {
						answerChoices.add(new AnswerDefinition(answer.toString()));
					}
				}
			}
		} catch (JSONException e) {
		}
		return null;
	}
}
