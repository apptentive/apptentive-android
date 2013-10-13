/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey.view;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.EditText;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.SurveyModule;
import com.apptentive.android.sdk.module.survey.SinglelineQuestion;
import com.apptentive.android.sdk.module.survey.SurveyState;

import java.util.ArrayList;
import java.util.Set;


/**
 * @author Sky Kelsey.
 */
public class TextSurveyQuestionView extends BaseSurveyQuestionView<SinglelineQuestion> {

	public TextSurveyQuestionView(Context context, final SinglelineQuestion question) {
		super(context, question);

		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		inflater.inflate(R.layout.apptentive_survey_question_singleline, getAnswerContainer());

		String instructionsText = question.isRequired() ? context.getString(R.string.apptentive_required) : context.getString(R.string.apptentive_optional);
		setInstructions(instructionsText);

		EditText answer = (EditText) findViewById(R.id.answer_text);
		Set<String> answers = SurveyModule.getInstance().getSurveyState().getAnswers(question.getId());
		if (answers.size() > 0) {
			answer.setText(new ArrayList<String>(answers).get(0));
		}
		answer.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			public void afterTextChanged(Editable editable) {
				String questionId = question.getId();
				SurveyState state = SurveyModule.getInstance().getSurveyState();
				Set<String> answers = state.getAnswers(questionId);
				if (answers.isEmpty() || (!answers.isEmpty() && !answers.contains(editable.toString()))) {
					state.clearAnswers(questionId);
					if (editable.length() != 0) {
						state.addAnswer(questionId, editable.toString());
					}
					updateValidationState();
					fireListener();
				}
			}
		});

		if (question.isMultiLine()) {
			answer.setGravity(Gravity.TOP);
			answer.setMinLines(5);
			answer.setMaxLines(12);
		} else {
			answer.setGravity(Gravity.CENTER_VERTICAL);
			answer.setMinLines(1);
			answer.setMaxLines(5);
		}
	}
}
