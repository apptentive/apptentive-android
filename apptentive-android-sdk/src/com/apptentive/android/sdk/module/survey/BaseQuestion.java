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
	private String instructions;

	private String[] answers;

	private boolean metricSent;

	public abstract int getType();

	protected BaseQuestion(JSONObject question) throws JSONException {
		this.id = question.getString("id");
		this.value = question.getString("value");
		this.required = question.optBoolean("required", false);
		if (!question.isNull("instructions")) {
			this.instructions = question.getString("instructions");
		} else {
			this.instructions = null;
		}
		metricSent = false;
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

	public String getInstructions() {
		return instructions;
	}

	protected void setAnswers(String... answers) {
		this.answers = answers;
	}

	public boolean isAnswered() {
		String[] answers = getAnswers();
		boolean answered = answers.length != 1 || !answers[0].equals("");
		return answered;
	}

	public String[] getAnswers() {
		if (answers == null || answers.length == 0) {
			return new String[]{""};
		}
		return answers;
	}

	public boolean isMetricSent() {
		return metricSent;
	}

	public void setMetricSent(boolean metricSent) {
		this.metricSent = metricSent;
	}
}
