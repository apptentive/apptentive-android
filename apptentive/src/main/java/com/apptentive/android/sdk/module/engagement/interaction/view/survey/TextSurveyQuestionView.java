/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.SinglelineQuestion;

import org.json.JSONException;


public class TextSurveyQuestionView extends BaseSurveyQuestionView<SinglelineQuestion> {

	EditText answer;

	public static TextSurveyQuestionView newInstance(SinglelineQuestion question) {

		TextSurveyQuestionView f = new TextSurveyQuestionView();

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
				question = new SinglelineQuestion(bundle.getString("question"));
			} catch (JSONException e) {

			}
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
													 Bundle savedInstanceState)  {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		Context contextThemeWrapper = new ContextThemeWrapper(getContext(), ApptentiveInternal.apptentiveTheme);
		LayoutInflater themedInflater = LayoutInflater.from(contextThemeWrapper);

		themedInflater.inflate(R.layout.apptentive_survey_question_singleline, getAnswerContainer(v));

		return v;
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		answer = (EditText) view.findViewById(R.id.answer_text);
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
			answer.setGravity(Gravity.TOP | Gravity.START);
			answer.setSingleLine(false);
			answer.setMinLines(5);
			answer.setMaxLines(12);
			answer.setSingleLine(false);
			answer.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
		} else {
			answer.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
			answer.setSingleLine(true);
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
