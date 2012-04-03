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
abstract public class BaseQuestion implements Question {
	private String id;
	private String value;
	private boolean required;

	public abstract int getType();

	protected BaseQuestion(JSONObject question) throws JSONException {
		this.id = question.getString("id");
		this.value = question.getString("value");
		this.required = question.optBoolean("required", false);
	}

	public String getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	public boolean isRequired() {
		return required;
	}
}
