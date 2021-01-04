/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model.survey;

import org.json.JSONException;

public class RangeQuestion extends BaseQuestion {

	private static final String KEY_MIN = "min";
	private static final String KEY_MAX = "max";
	private static final String KEY_MIN_LABEL = "min_label";
	private static final String KEY_MAX_LABEL = "max_label";
	private static final String DEFAULT_MIN_RANGE_LABEL = "Lowest";
	private static final String DEFAULT_MAX_RANGE_LABEL = "Highest";

	private static final int DEFAULT_MIN = 0;
	private static final int DEFAULT_MAX = 10;

	public RangeQuestion(String json) throws JSONException {
		super(json);
	}

	public int getType() {
		return QUESTION_TYPE_RANGE;
	}

	public int getMin() {
		return optInt(KEY_MIN, DEFAULT_MIN);
	}

	public int getMax() {
		return optInt(KEY_MAX, DEFAULT_MAX);
	}

	public String getMinLabel() {
		return optString(KEY_MIN_LABEL, DEFAULT_MIN_RANGE_LABEL);
	}

	public String getMaxLabel() {
		return optString(KEY_MAX_LABEL, DEFAULT_MAX_RANGE_LABEL);
	}
}
