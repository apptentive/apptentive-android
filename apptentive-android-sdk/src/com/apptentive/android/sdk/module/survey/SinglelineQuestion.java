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
public class SinglelineQuestion extends BaseQuestion {

	public SinglelineQuestion(String json) throws JSONException {
		super(json);
	}

	public int getType() {
		return QUESTION_TYPE_SINGLELINE;
	}
}
