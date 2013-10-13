/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.SurveyModule;
import com.apptentive.android.sdk.module.survey.AnswerDefinition;
import com.apptentive.android.sdk.module.survey.MultichoiceQuestion;
import com.apptentive.android.sdk.util.Util;

import java.util.*;

/**
 * @author Sky Kelsey.
 */
public class MultichoiceSurveyQuestionView extends BaseSurveyQuestionView<MultichoiceQuestion> {

	protected Map<String, CheckboxChoice> answersChoices;
	protected Map<CheckboxChoice, String> answersChoicesReverse;

	public MultichoiceSurveyQuestionView(Context context, MultichoiceQuestion question) {
		super(context, question);
		answersChoices = new HashMap<String, CheckboxChoice>();
		answersChoicesReverse = new HashMap<CheckboxChoice, String>();

		List<AnswerDefinition> answerDefinitions = question.getAnswerChoices();

		Set<String> answers = SurveyModule.getInstance().getSurveyState().getAnswers(question.getId());

		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		inflater.inflate(R.layout.apptentive_survey_question_multichoice, getAnswerContainer());
		LinearLayout choiceContainer = (LinearLayout) findViewById(R.id.choice_container);

		for (int i = 0; i < answerDefinitions.size(); i++) {
			AnswerDefinition answerDefinition = answerDefinitions.get(i);
			final CheckboxChoice choice = new CheckboxChoice(context, answerDefinition.getValue());
			if (answers.contains(answerDefinition.getId())) {
				choice.post(new Runnable() {
					@Override
					public void run() {
						choice.toggle();
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

			if (i != answerDefinitions.size() - 1) {
				FrameLayout sep = new FrameLayout(context);
				sep.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 1));
				sep.setBackgroundColor(context.getResources().getColor(R.color.apptentive_survey_question_separator));
				choiceContainer.addView(sep);
			}
		}
	}

	/**
	 * Override to change the behavior of clicking this.
	 */
	protected void choiceClicked(CheckboxChoice choice) {

		String clickedId = answersChoicesReverse.get(choice);

		Set<String> answers = SurveyModule.getInstance().getSurveyState().getAnswers(question.getId());
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
		SurveyModule.getInstance().getSurveyState().setAnswers(question.getId(), checkedChoices);
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
		SurveyModule.getInstance().getSurveyState().clearAnswers(question.getId());
		for (String id : answersChoices.keySet()) {
			if (answersChoices.get(id).isChecked()) {
				answersChoices.get(id).toggle();
			}
		}
	}
}
