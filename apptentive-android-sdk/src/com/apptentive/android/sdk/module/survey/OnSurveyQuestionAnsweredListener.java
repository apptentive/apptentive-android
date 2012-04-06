/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

/**
 * @author Sky Kelsey.
 */
public interface OnSurveyQuestionAnsweredListener<T> {
	@SuppressWarnings("unchecked")
	public void onAnswered(T view);
}
