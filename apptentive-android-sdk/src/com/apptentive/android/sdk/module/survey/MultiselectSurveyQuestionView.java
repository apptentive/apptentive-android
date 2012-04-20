/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey.
 */
public class MultiselectSurveyQuestionView extends MultichoiceSurveyQuestionView {

	protected int minSelections;
	protected int maxSelections;

	public MultiselectSurveyQuestionView(Context context, MultiselectQuestion question) {
		super(context, question);
		this.minSelections = question.getMinSelections();
		this.maxSelections = question.getMaxSelections();
		updateInstructionsColor();
	}

	/**
	 * Override to change the behavior of clicking this.
	 * @param choice
	 */
	protected void choiceClicked(CheckableChoice choice) {
		if(canToggle(choice)) {
			choice.toggle();
			List<String> checkedChoices = new ArrayList<String>();
			for (String id : answers.keySet()) {
				if(answers.get(id).isChecked()) {
					checkedChoices.add(id);
				}
			}
			question.setAnswers((String[]) checkedChoices.toArray(new String[]{}));
			updateInstructionsColor();
		} else {
			flashInstructionsRed();
		}
		fireListener();
	}

	protected void updateInstructionsColor() {
		if(question != null && !question.isAnswered()) {
			instructionsTextView.setTextColor(Color.RED);
		} else {
			instructionsTextView.setTextColor(Color.GRAY);
		}
	}

	private boolean canToggle(CheckableChoice choice) {
		int selectedChoices = countSelectedChoices();
		boolean allowToggle = true;
		if(minSelections != -1 && choice.isChecked() && selectedChoices <= minSelections) {
			allowToggle = false;
		}
		if(maxSelections != -1 && !choice.isChecked() && selectedChoices >= maxSelections) {
			allowToggle = false;
		}
		return allowToggle;
	}
}
