/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.graphics.Color;
import com.apptentive.android.sdk.SurveyModule;

import java.util.HashSet;
import java.util.Set;

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
			Set<String> checkedChoices = new HashSet<String>();
			for (String id : answersChoices.keySet()) {
				if(answersChoices.get(id).isChecked()) {
					checkedChoices.add(id);
				}
			}
			SurveyModule.getInstance().getSurveyState().setAnswers(question.getId(), checkedChoices);
			updateInstructionsColor();
		} else {
			flashInstructionsRed();
		}
		fireListener();
	}

	protected void updateInstructionsColor() {
		if(!SurveyModule.getInstance().getSurveyState().isAnswered(question.getId())) {
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
