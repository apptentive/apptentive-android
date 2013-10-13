/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import org.json.JSONException;

/**
 * @author Sky Kelsey.
 */
public class MultiselectQuestion extends MultichoiceQuestion {

	protected MultiselectQuestion(String question) throws JSONException {
		super(question);
	}

	public int getType() {
		return QUESTION_TYPE_MULTISELECT;
	}

	public int getMaxSelections() {
		return optInt(KEY_MAX_SELECTIONS, getAnswerChoices().size());
	}
}
