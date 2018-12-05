/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.app.Activity;
import android.os.Bundle;

import com.apptentive.android.sdk.module.engagement.interaction.model.survey.MultiselectQuestion;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONArray;
import org.json.JSONException;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class MultiselectSurveyQuestionView extends MultichoiceSurveyQuestionView implements SurveyQuestionChoice.OnCheckedChangeListener {

	public static MultiselectSurveyQuestionView newInstance(MultiselectQuestion question) {
		MultiselectSurveyQuestionView f = new MultiselectSurveyQuestionView();
		Bundle b = new Bundle();
		b.putString("question", question.toString());
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		if (bundle != null) {
			try {
				// Must cast question to "MultiselectQuestion" each time it's used.
				question = new MultiselectQuestion(bundle.getString("question"));
			} catch (JSONException e) {
				logException(e);
			}
		}
	}

	@Override
	public void onCheckChanged(SurveyQuestionChoice surveyQuestionChoice, boolean isChecked) {
		int checkedIndex = surveyQuestionChoice.getIndex();
		if (isChecked) {
			selectedChoices.add(checkedIndex);
		} else {
			selectedChoices.remove(checkedIndex);
		}
		if (getContext() instanceof Activity) {
			Util.hideSoftKeyboard(getContext(), MultiselectSurveyQuestionView.this.getView());
		}
		fireListener();
	}

	@Override
	public boolean isValid() {
		// Make sure all questions are valid.
		for (int i = 0; i < choiceContainer.getChildCount(); i++) {
			SurveyQuestionChoice surveyQuestionChoice = (SurveyQuestionChoice) choiceContainer.getChildAt(i);
			if (!surveyQuestionChoice.isValid(question.isRequired())) {
				return false;
			}
		}
		// Then make sure the number of answers is valid.
		int checkedBoxes = selectedChoices.size();

		// Cast to specific question type. Yes, this is a bit hacky.
		MultiselectQuestion question = (MultiselectQuestion) super.question;
		// If it is answered at all, it must be answered properly.
		return
			(!question.isRequired() && checkedBoxes == 0) ||
				((question.getMinSelections() <= checkedBoxes) && (checkedBoxes <= question.getMaxSelections()));
	}

	@Override
	public Object getAnswer() {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < choiceContainer.getChildCount(); i++) {
			SurveyQuestionChoice surveyQuestionChoice = (SurveyQuestionChoice) choiceContainer.getChildAt(i);
			if (surveyQuestionChoice.isChecked()) {
				jsonArray.put(surveyQuestionChoice.getAnswer());
			}
		}
		if (jsonArray.length() > 0) {
			return jsonArray;
		}
		return null;
	}
}
