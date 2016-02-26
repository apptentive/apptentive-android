/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.content.Context;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.SurveyState;
import com.apptentive.android.sdk.module.survey.OnSurveyQuestionAnsweredListener;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.Question;

abstract public class BaseSurveyQuestionView<Q extends Question> extends FrameLayout {

	protected Q question;
	protected SurveyState surveyState;

	protected OnSurveyQuestionAnsweredListener listener;

	protected View required;
	protected View dash;
	protected TextView instructions;

	protected final Context contextThemeWrapper;
	protected final LayoutInflater inflater;

	protected BaseSurveyQuestionView(Context context, SurveyState surveyState, Q question) {
		super(context);
		this.question = question;
		this.surveyState = surveyState;

		// Required to remove focus from any EditTexts.
//		setFocusable(true);
//		setFocusableInTouchMode(true);

		contextThemeWrapper = new ContextThemeWrapper(context, ApptentiveInternal.apptentiveTheme);
		inflater = LayoutInflater.from(contextThemeWrapper);
		inflater.inflate(R.layout.apptentive_survey_question_base, this);

		required = findViewById(R.id.question_required);
		dash = findViewById(R.id.question_dash);
		instructions = (TextView) findViewById(R.id.question_instructions);

		TextView title = (TextView) findViewById(R.id.question_title);
		title.setText(question.getValue());

		String instructionsText = question.getInstructions();
		setInstructions(instructionsText);
	}

	protected void setInstructions(String instructionsText) {
		boolean hasInstructions = !TextUtils.isEmpty(instructionsText);

		View required = findViewById(R.id.question_required);
		if (question.isRequired()) {
			required.setVisibility(View.VISIBLE);
		} else {
			required.setVisibility(View.GONE);
		}

		if (question.isRequired() && hasInstructions) {
			dash.setVisibility(View.VISIBLE);
		} else {
			dash.setVisibility(View.GONE);
		}

		if (hasInstructions) {
			instructions.setText(instructionsText);
			instructions.setVisibility(View.VISIBLE);
		} else {
			instructions.setVisibility(View.GONE);
		}
	}

	protected LinearLayout getAnswerContainer() {
		return (LinearLayout) findViewById(R.id.answer_container);
	}

	public void setOnSurveyQuestionAnsweredListener(OnSurveyQuestionAnsweredListener listener) {
		this.listener = listener;
	}

	/**
	 * Always call this when the answer value changes.
	 */
	protected void fireListener() {
		if (listener != null) {
			listener.onAnswered();
		}
	}

	public void updateValidationState() {
		View validationFailedBorder = findViewById(R.id.validation_failed_border);
		if (question != null && !surveyState.isQuestionValid(question)) {
			validationFailedBorder.setVisibility(View.VISIBLE);
		} else {
			validationFailedBorder.setVisibility(View.INVISIBLE);
		}
	}
}
