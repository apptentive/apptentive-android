/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import com.apptentive.android.sdk.module.engagement.interaction.view.survey.BaseSurveyQuestionView;


public interface OnSurveyQuestionAnsweredListener {
	void onAnswered(BaseSurveyQuestionView questionView);
}
