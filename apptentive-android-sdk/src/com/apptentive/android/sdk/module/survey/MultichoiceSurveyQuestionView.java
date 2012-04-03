/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sky Kelsey.
 */
public class MultichoiceSurveyQuestionView extends SurveyItemView {

	protected Map<String, CheckableChoice> answers;
	protected int maxChoices = 1; // Default

	public MultichoiceSurveyQuestionView(Context context) {
		super(context);
	}

	@Override
	protected void initView() {
		super.initView();
		answers = new HashMap<String, CheckableChoice>();
	}

	public void setAnswers(List<AnswerDefinition> answerDefinitions) {
		for (int i = 0; i < answerDefinitions.size(); i++) {
			addSeparator();
			AnswerDefinition answerDefinition = answerDefinitions.get(i);

			final CheckableChoice choice = new CheckableChoice(appContext);
			choice.setText(answerDefinition.getValue());
			choice.setClickable(true);
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
	 * @param choice
	 */
	protected void choiceClicked(CheckableChoice choice) {
		if(countSelectedChoices() != 0) {
			clearAllChoices();
		}
		choice.toggle();
		fireListener();
	}

	protected int countSelectedChoices() {
		int ret = 0;
		for(String id : answers.keySet()) {
			if(answers.get(id).isChecked()) {
				ret++;
			}
		}
		return ret;
	}

	protected void clearAllChoices() {
		for(String id : answers.keySet()) {
			if(answers.get(id).isChecked()) {
				answers.get(id).toggle();
			}
		}
	}

	public Map<String, Boolean> getAnswers() {
		Map<String, Boolean> ret = new HashMap<String, Boolean>();
		for( String id : answers.keySet()){
			ret.put(id, answers.get(id).isChecked());
		}
		return ret;
	}
}
