/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import com.apptentive.android.sdk.module.survey.OnSurveyQuestionAnsweredListener;

public interface SurveyQuestionView {

	void setOnSurveyQuestionAnsweredListener(OnSurveyQuestionAnsweredListener listener);

	String getQuestionId();

	/**
	 * @return true if this question can be submitted, else false.
	 */
	boolean isValid();

	void updateValidationState(boolean valid);

	/**
	 * Extract the answer from the question's view state.
	 *
	 * @return An Object that could be a JSONArray, or String, depending on the question type and the number of answers.
	 */
	Object getAnswer();

	/**
	 * Error message for the case if required question is not answered
	 */
	String getErrorMessage();

	boolean didSendMetric();

	void setSentMetric(boolean sent);
}
