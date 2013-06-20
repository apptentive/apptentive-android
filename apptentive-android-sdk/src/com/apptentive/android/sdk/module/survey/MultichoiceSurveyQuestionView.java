/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import com.apptentive.android.sdk.SurveyModule;
import com.apptentive.android.sdk.util.Util;

import java.util.*;

/**
 * @author Sky Kelsey.
 */
public class MultichoiceSurveyQuestionView extends SurveyItemView<MultichoiceQuestion> {

	protected Map<String, CheckableChoice> answersChoices;

	public MultichoiceSurveyQuestionView(Context context, MultichoiceQuestion question) {
		super(context, question);
		setAnswersChoices(question.getAnswerChoices());
	}

	@Override
	protected void initView() {
		super.initView();
		answersChoices = new HashMap<String, CheckableChoice>();
	}

	protected void setAnswersChoices(List<AnswerDefinition> answerDefinitions) {
		if(answerDefinitions == null) {
			return;
		}
		Set<String> answers = SurveyModule.getInstance().getSurveyState().getAnswers(question.getId());
		for (AnswerDefinition answerDefinition : answerDefinitions) {
			addSeparator();

			final CheckableChoice choice = new CheckableChoice(appContext);
			choice.setText(answerDefinition.getValue());
			choice.setClickable(true);
			if(answers.contains(answerDefinition.getId())) {
				choice.toggle();
			}
			choice.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					if(getContext() instanceof Activity) {
						Util.hideSoftKeyboard((Activity)getContext(), MultichoiceSurveyQuestionView.this);
					}
					choiceClicked(choice);
				}
			});
			answersChoices.put(answerDefinition.getId(), choice);
			questionView.addView(choice);
		}
	}

	/**
	 * Override to change the behavior of clicking this.
	 *
	 * @param choice
	 */
	protected void choiceClicked(CheckableChoice choice) {
		if (countSelectedChoices() != 0) {
			clearAllChoices();
		}
		choice.toggle();
		Set<String> checkedChoices = new HashSet<String>();
		for (String id : answersChoices.keySet()) {
			if(answersChoices.get(id).isChecked()) {
				checkedChoices.add(id);
			}
		}
		SurveyModule.getInstance().getSurveyState().setAnswers(question.getId(), checkedChoices);
		updateInstructionsColor();
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
