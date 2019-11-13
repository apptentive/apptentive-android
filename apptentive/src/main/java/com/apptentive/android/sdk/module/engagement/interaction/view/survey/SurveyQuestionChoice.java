/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.AnswerDefinition;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.Question;

import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONObject;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

/**
 * Used by both Multichoice and Multiselect survey questions, since they are 99% the same UI.
 */
public class SurveyQuestionChoice extends FrameLayout implements CompoundButton.OnCheckedChangeListener, TextWatcher {

	private final int index;
	private final String answerId;
	private final boolean isOtherType;

	private final CheckBox checkBox;
	private final TextInputLayout otherTextInputLayout;
	private final EditText otherTextInput;

	private OnCheckedChangeListener onCheckChangedListener;
	private OnOtherTextChangedListener onOtherTextChangedListener;

	public SurveyQuestionChoice(Context context, LayoutInflater inflater, AnswerDefinition answerDefinition, int questionType, int index) {
		super(context);

		this.index = index;
		answerId = answerDefinition.getId();
		isOtherType = answerDefinition.getType().equals(AnswerDefinition.TYPE_OTHER);


		switch (questionType) {
			case Question.QUESTION_TYPE_MULTICHOICE:
				inflater.inflate(R.layout.apptentive_survey_question_multichoice_choice, this);
				break;
			case Question.QUESTION_TYPE_MULTISELECT:
			default:
				inflater.inflate(R.layout.apptentive_survey_question_multiselect_choice, this);
				break;
		}
		checkBox = (CheckBox) findViewById(R.id.checkbox);
		otherTextInputLayout = (TextInputLayout) findViewById(R.id.other_text_input_layout);
		otherTextInput = (EditText) findViewById(R.id.other_edit_text);

		checkBox.setText(answerDefinition.getValue());
		if (isOtherType) {
			otherTextInputLayout.setHint(answerDefinition.getHint());
		}

		checkBox.setOnCheckedChangeListener(this);

		if (isOtherType) {
			otherTextInput.addTextChangedListener(this);
		}
	}

	public int getIndex() {
		return index;
	}

	public String getAnswerId() {
		return answerId;
	}

	public boolean isOtherType() {
		return isOtherType;
	}

	public String getOtherText() {
		return otherTextInput.getText().toString().trim();
	}

	public void setOtherText(String otherText) {
		otherTextInput.setText(otherText);
	}

	/**
	 * An answer can only be invalid if it's checked, the question is required, and the type is "other", but nothing was typed.
	 * All answers must be valid to submit, in addition to whatever logic the question applies.
	 */
	public boolean isValid(boolean questionIsRequired) {
		// If required and checked, other types must have text
		if (questionIsRequired && isChecked() && isOtherType && (getOtherText().length() < 1)) {
			otherTextInputLayout.setError(" ");
			return false;
		}
		otherTextInputLayout.setError(null);
		return true;
	}

	private void updateOtherState(boolean showOther) {
		if (showOther) {
			otherTextInputLayout.setVisibility(View.VISIBLE);
			otherTextInput.post(new Runnable() { // TODO: replace with DispatchQueue
				@Override
				public void run() {
					otherTextInput.requestFocus();
					((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(otherTextInput, 0);
				}
			});
		} else {
			otherTextInputLayout.setVisibility(View.GONE);
		}
	}

	public boolean isChecked() {
		return checkBox.isChecked();
	}

	public void setChecked(boolean checked) {
		checkBox.setChecked(checked);
		if (isOtherType) {
			updateOtherState(checked);
		}
	}

	public Object getAnswer() {
		try {
			JSONObject answer = new JSONObject();
			answer.put("id", answerId);
			if (isOtherType()) {
				answer.put("value", getOtherText());
			}
			return answer;
		} catch (Exception e) {
			ApptentiveLog.e(e, "Error producing survey answer.");
			logException(e);
		}
		return null;
	}

	public void setOnCheckChangedListener(OnCheckedChangeListener onCheckChangedListener) {
		this.onCheckChangedListener = onCheckChangedListener;
	}

	public interface OnCheckedChangeListener {
		void onCheckChanged(SurveyQuestionChoice choice, boolean isChecked);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isOtherType) {
			updateOtherState(isChecked);
		}
		if (onCheckChangedListener != null) {
			onCheckChangedListener.onCheckChanged(SurveyQuestionChoice.this, isChecked);
		}
	}

	public interface OnOtherTextChangedListener {
		void onOtherTextChanged(SurveyQuestionChoice choice, String text);
	}

	public void setOnOtherTextChangedListener(OnOtherTextChangedListener onOtherTextChangedListener) {
		this.onOtherTextChangedListener = onOtherTextChangedListener;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		String result = s.toString().trim();
		if (onOtherTextChangedListener != null) {
			onOtherTextChangedListener.onOtherTextChanged(this, result);
		}
	}
}
