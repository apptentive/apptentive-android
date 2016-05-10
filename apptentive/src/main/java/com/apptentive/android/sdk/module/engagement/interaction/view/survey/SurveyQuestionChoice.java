/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.AnswerDefinition;

/**
 * Used by both Multichoice and Multiselect survey questions, since they are 99% the same UI.
 */
public class SurveyQuestionChoice extends FrameLayout {

	protected AnswerDefinition answerDefinition;
	protected CheckBox checkBox;
	protected TextInputLayout otherTextInputLayout;
	protected EditText otherTextInput;
	protected boolean isOtherType;

	private OnCheckedChangeListener onCheckChangedListener;

	public SurveyQuestionChoice(Context context, AnswerDefinition answerDefinition) {
		super(context);

		Context contextThemeWrapper = new ContextThemeWrapper(getContext(), ApptentiveInternal.getInstance().getApptentiveTheme());
		LayoutInflater themedInflater = LayoutInflater.from(contextThemeWrapper);

		themedInflater.inflate(R.layout.apptentive_survey_question_multichoice_choice, this);
		checkBox = (CheckBox) findViewById(R.id.checkbox);
		otherTextInputLayout = (TextInputLayout) findViewById(R.id.other_text_input_layout);
		otherTextInput = (EditText) findViewById(R.id.other_edit_text);

		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isOtherType) {
					updateOtherState(isChecked);
				}
				if (onCheckChangedListener != null) {
					onCheckChangedListener.onCheckChanged(SurveyQuestionChoice.this, isChecked);
				}
			}
		});

		setAnswerDefinition(answerDefinition);
	}

	public void setAnswerDefinition(AnswerDefinition answerDefinition) {
		this.answerDefinition = answerDefinition;
		isOtherType = answerDefinition.getType().equals(AnswerDefinition.TYPE_OTHER);
		checkBox.setText(answerDefinition.getValue());
		if (isOtherType) {
			otherTextInputLayout.setHint(answerDefinition.getHint());
		}
	}

	public void setOtherText(String otherText) {
		otherTextInput.setText(otherText);
	}

	private void updateOtherState(boolean showOther) {
		if (showOther) {
			otherTextInputLayout.setVisibility(View.VISIBLE);
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

	public void setOnCheckChangedListener(OnCheckedChangeListener onCheckChangedListener) {
		this.onCheckChangedListener = onCheckChangedListener;
	}

	public interface OnCheckedChangeListener {
		void onCheckChanged(SurveyQuestionChoice choice, boolean isChecked);
	}
}
