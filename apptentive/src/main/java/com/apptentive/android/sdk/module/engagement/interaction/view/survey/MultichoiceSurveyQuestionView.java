/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.app.Activity;
import android.content.Context;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.AnswerDefinition;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.MultichoiceQuestion;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.SurveyState;
import com.apptentive.android.sdk.util.Util;

import java.util.*;

public class MultichoiceSurveyQuestionView extends BaseSurveyQuestionView<MultichoiceQuestion> {

	protected Map<String, CheckboxChoice> answersChoices;
	protected Map<CheckboxChoice, String> answersChoicesReverse;

	public MultichoiceSurveyQuestionView(Context context, SurveyState surveyState, MultichoiceQuestion question) {
		super(context, surveyState, question);
		answersChoices = new HashMap<String, CheckboxChoice>();
		answersChoicesReverse = new HashMap<CheckboxChoice, String>();

		List<AnswerDefinition> answerDefinitions = question.getAnswerChoices();

		Set<String> answers = surveyState.getAnswers(question.getId());

		final Context contextThemeWrapper = new ContextThemeWrapper(context, ApptentiveInternal.apptentiveTheme);
		LayoutInflater inflater = LayoutInflater.from(contextThemeWrapper);
		View questionView = inflater.inflate(R.layout.apptentive_survey_question_multichoice, getAnswerContainer());

		LinearLayout choiceContainer = (LinearLayout) questionView.findViewById(R.id.choice_container);

		for (int i = 0; i < answerDefinitions.size(); i++) {
			AnswerDefinition answerDefinition = answerDefinitions.get(i);
			final CheckboxChoice choice = new CheckboxChoice(context, answerDefinition.getValue());
			if (answers.contains(answerDefinition.getId())) {
				choice.post(new Runnable() {
					@Override
					public void run() {
						choice.check();
					}
				});
			}
			choice.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					if (getContext() instanceof Activity) {
						Util.hideSoftKeyboard((Activity) getContext(), MultichoiceSurveyQuestionView.this);
					}
					choiceClicked(choice);
				}
			});
			answersChoices.put(answerDefinition.getId(), choice);
			answersChoicesReverse.put(choice, answerDefinition.getId());
			choiceContainer.addView(choice);
		}
	}

	/**
	 * Override to change the behavior of clicking this.
	 */
	protected void choiceClicked(CheckboxChoice choice) {

		String clickedId = answersChoicesReverse.get(choice);

		Set<String> answers = surveyState.getAnswers(question.getId());
		boolean alreadyAnswered = answers != null && answers.contains(clickedId);
		if (alreadyAnswered) {
			choice.toggle();
		} else {
			if (countSelectedChoices() != 0) {
				clearAllChoices();
			}
			choice.toggle();
		}
		Set<String> checkedChoices = new HashSet<String>();
		for (String id : answersChoices.keySet()) {
			if (answersChoices.get(id).isChecked()) {
				checkedChoices.add(id);
			}
		}
		surveyState.setAnswers(question.getId(), checkedChoices);
		updateValidationState();
		requestFocus();
		fireListener();
	}

	protected int countSelectedChoices() {
		int ret = 0;
		for (String id : answersChoices.keySet()) {
			if (answersChoices.get(id).isChecked()) {
				ret++;
			}
		}
		return ret;
	}

	protected void clearAllChoices() {
		surveyState.clearAnswers(question.getId());
		for (String id : answersChoices.keySet()) {
			if (answersChoices.get(id).isChecked()) {
				answersChoices.get(id).toggle();
			}
		}
	}
}
