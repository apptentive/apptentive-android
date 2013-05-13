/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

/**
 * This interface is provided so you can get a callback when a survey has been completed.
 *
 * @author Sky Kelsey
 */
public interface OnSurveyCompletedListener {

	/**
	 * Callback called when a survey has been completed. This is not called if the survey was skipped
	 * @param completedSurvey	True if the end user completed the survey, false if they skipped it.
	 */
	public void onSurveyCompletedListener(boolean completedSurvey);
}
