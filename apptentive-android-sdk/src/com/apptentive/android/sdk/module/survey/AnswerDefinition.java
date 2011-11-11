/*
 * AnswerDefinition.java
 *
 * Created by Sky Kelsey on 2011-10-08.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.module.survey;

import org.json.JSONException;
import org.json.JSONObject;

public class AnswerDefinition {

	private String id;
	private String value;

	public AnswerDefinition(JSONObject answer) throws JSONException {
		this.id = answer.getString("id");
		this.value = answer.getString("value");
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
