/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.view.View;

import java.util.*;

/**
 * @author Sky Kelsey.
 */
public class MultichoiceSurveyQuestionView extends SurveyItemView<MultichoiceQuestion> {

	protected Map<String, CheckableChoice> answers;

	public MultichoiceSurveyQuestionView(Context context, MultichoiceQuestion question) {
		super(context, question);
		setAnswers(question.getAnswerChoices());
	}

	@Override
	protected void initView() {
		super.initView();
		answers = new HashMap<String, CheckableChoice>();
	}

	protected void setAnswers(List<AnswerDefinition> answerDefinitions) {
		for (int i = 0; i < answerDefinitions.size(); i++) {
			addSeparator();
			AnswerDefinition answerDefinition = answerDefinitions.get(i);

			final CheckableChoice choice = new CheckableChoice(appContext);
			choice.setText(answerDefinition.getValue());
			choice.setClickable(true);
			if(Arrays.asList(question.getAnswers()).contains(answerDefinition.getId())) {
				choice.toggle();
			}
			choice.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					choiceClicked(choice);
				}
			});
			answers.put(answerDefinition.getId(), choice);
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
		List<String> checkedChoices = new ArrayList<String>();
		for (String id : answers.keySet()) {
			if(answers.get(id).isChecked()) {
				checkedChoices.add(id);
			}
		}
		question.setAnswers((String[]) checkedChoices.toArray(new String[]{}));
		updateInstructionsColor();
		fireListener();
	}

	protected int countSelectedChoices() {
		int ret = 0;
		for (String id : answers.keySet()) {
			if (answers.get(id).isChecked()) {
				ret++;
			}
		}
		return ret;
	}

	protected void clearAllChoices() {
		question.setAnswers();
		for (String id : answers.keySet()) {
			if (answers.get(id).isChecked()) {
				answers.get(id).toggle();
			}
		}
	}
}
