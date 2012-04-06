/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey.
 */
public class MultiselectSurveyQuestionView extends MultichoiceSurveyQuestionView {
	public MultiselectSurveyQuestionView(Context context, MultiselectQuestion question) {
		super(context, question);
		setMaxChoices(question.getMaxSelections());
	}

	/**
	 * Override to change the behavior of clicking this.
	 * @param choice
	 */
	protected void choiceClicked(CheckableChoice choice) {
		if(choice.isChecked() || countSelectedChoices() < maxChoices) {
			choice.toggle();
			List<String> checkedChoices = new ArrayList<String>();
			for (String id : answers.keySet()) {
				if(answers.get(id).isChecked()) {
					checkedChoices.add(id);
				}
			}
			question.setAnswers((String[]) checkedChoices.toArray(new String[]{}));
			fireListener();
		} else {
			choice.warn();
		}
	}
}
