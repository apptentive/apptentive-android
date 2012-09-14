/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

/**
 * This interface is provided so you can get a callback when a survey has been fetched.
 *
 * @author Sky Kelsey.
 */
public interface OnSurveyFetchedListener {

	/**
	 * Callback called when a survey has been fetched.
	 *
	 * @param success True only if the survey was fetched successfully.
	 */
	public void onSurveyFetched(boolean success);
}
