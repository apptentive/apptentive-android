/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey.view;

import android.content.Context;
import com.apptentive.android.sdk.SurveyModule;
import com.apptentive.android.sdk.module.survey.MultiselectQuestion;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Sky Kelsey.
 */
public class MultiselectSurveyQuestionView2 extends MultichoiceSurveyQuestionView2 {

	protected int minSelections;
	protected int maxSelections;

	public MultiselectSurveyQuestionView2(Context context, MultiselectQuestion question) {
		super(context, question);
		this.minSelections = question.getMinSelections();
		this.maxSelections = question.getMaxSelections();
		updateValidationState();
	}

	/**
	 * Override to change the behavior of clicking this.
	 *
	 * @param choice
	 */
	protected void choiceClicked(CheckboxChoice choice) {
		choice.toggle();
		Set<String> checkedChoices = new HashSet<String>();
		for (String id : answersChoices.keySet()) {
			if (answersChoices.get(id).isChecked()) {
				checkedChoices.add(id);
			}
		}
		SurveyModule.getInstance().getSurveyState().setAnswers(question.getId(), checkedChoices);
		updateValidationState();
		requestFocus();
		fireListener();
	}
}
