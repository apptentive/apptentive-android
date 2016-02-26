/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.content.Context;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.SinglelineQuestion;


public class TextSurveyQuestionView extends BaseSurveyQuestionView<SinglelineQuestion> {

	EditText answer;

	public TextSurveyQuestionView(Context context, final SinglelineQuestion question) {
		super(context, question);

		final Context contextThemeWrapper = new ContextThemeWrapper(context, ApptentiveInternal.apptentiveTheme);
		LayoutInflater inflater = LayoutInflater.from(contextThemeWrapper);
		inflater.inflate(R.layout.apptentive_survey_question_singleline, getAnswerContainer());

		answer = (EditText) findViewById(R.id.answer_text);
		answer.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			public void afterTextChanged(Editable editable) {
				fireListener();
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

	@Override
	public boolean isValid() {
		return !question.isRequired() || !TextUtils.isEmpty(answer.getText().toString());
	}

	@Override
	public Object getAnswer() {
		String value = answer.getText().toString().trim();
		if (TextUtils.isEmpty(value)) {
			return null;
		} else {
			return value;
		}
	}
}
