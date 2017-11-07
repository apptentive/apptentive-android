/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.SinglelineQuestion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class TextSurveyQuestionView extends BaseSurveyQuestionView<SinglelineQuestion> implements TextWatcher {

	TextInputLayout answerTextInputLayout;
	EditText answer;

	private final static String SURVEY_ANSWER_FOCUS = "answerFocus";
	private boolean isFocused;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		inflater.inflate(R.layout.apptentive_survey_question_singleline, getAnswerContainer(v));

		return v;
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		isFocused = (savedInstanceState == null) ? false : savedInstanceState.getBoolean(SURVEY_ANSWER_FOCUS, false);
		answerTextInputLayout = (TextInputLayout) view.findViewById(R.id.answer_text_input_layout);
		answer = (EditText) view.findViewById(R.id.answer_text);

		answer.removeTextChangedListener(this);

		String hint = question.getFreeformHint();
		if (!TextUtils.isEmpty(hint)) {
			answerTextInputLayout.setHint(hint);
			answerTextInputLayout.setContentDescription(hint);
			answer.setContentDescription(hint);
		}

		answer.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean focus) {
				isFocused = focus;
			}
		});

		if (question.isMultiLine()) {
			answerTextInputLayout.setGravity(Gravity.TOP | Gravity.START);
			answer.setGravity(Gravity.TOP | Gravity.START);
			answer.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
			answer.setMinLines(5);
			answer.setMaxLines(12);
			answer.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
		} else {
			answerTextInputLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
			answer.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
			answer.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);
			answer.setMinLines(1);
			answer.setMaxLines(5);
		}

		if (isFocused) {
			answer.post(new Runnable() {
				public void run() {
					answer.requestFocus();
				}
			});
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		answer.addTextChangedListener(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(SURVEY_ANSWER_FOCUS, isFocused);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean isValid() {

		boolean valid = !question.isRequired() || !TextUtils.isEmpty(answer.getText().toString());
		if (!valid) {
			answerTextInputLayout.setError(" ");
		} else {
			answerTextInputLayout.setError(null);
		}
		return valid;
	}

	@Override
	public Object getAnswer() {
		String value = answer.getText().toString().trim();
		try {
			if (!TextUtils.isEmpty(value)) {
				JSONArray jsonArray = new JSONArray();
				JSONObject jsonObject = new JSONObject();
				jsonArray.put(jsonObject);
				jsonObject.put("value", value);
				return jsonArray;
			}
		} catch (JSONException e) {
			// Return null;
		}
		return null;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		fireListener();
	}
}
