/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.content.Context;

/**
 * @author Sky Kelsey.
 */
public class MultiselectSurveyQuestionView extends MultichoiceSurveyQuestionView {
	public MultiselectSurveyQuestionView(Context context) {
		super(context);
	}

	public void setMaxChoices(int maxChoices) {
		this.maxChoices = maxChoices;
	}

	/**
	 * Override to change the behavior of clicking this.
	 * @param choice
	 */
	protected void choiceClicked(CheckableChoice choice) {
		if(choice.isChecked() || countSelectedChoices() < maxChoices) {
			choice.toggle();
			fireListener();
		} else {
			choice.warn();
		}
	}
}
