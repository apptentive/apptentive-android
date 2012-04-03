/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey.
 */
public class MultiselectQuestion extends MultichoiceQuestion {

	protected MultiselectQuestion(JSONObject question) throws JSONException {
		super(question);
		this.maxSelections = question.getInt("max_selections");
	}

	public int getType() {
		return QUESTION_TYPE_MULTISELECT;
	}
}
