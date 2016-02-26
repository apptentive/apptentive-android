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
import com.apptentive.android.sdk.module.survey.OnSurveyQuestionAnsweredListener;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.Question;

abstract public class BaseSurveyQuestionView<Q extends Question> extends FrameLayout {

	protected Q question;
	private OnSurveyQuestionAnsweredListener listener;

	protected View requiredView;
	protected View dashView;
	protected TextView instructionsView;

	protected final Context contextThemeWrapper;
	protected final LayoutInflater inflater;

	private View validationFailedBorder;

	protected BaseSurveyQuestionView(Context context, Q question) {
		super(context);
		this.question = question;

		// Required to remove focus from any EditTexts.
//		setFocusable(true);
//		setFocusableInTouchMode(true);

		contextThemeWrapper = new ContextThemeWrapper(context, ApptentiveInternal.apptentiveTheme);
		inflater = LayoutInflater.from(contextThemeWrapper);
		inflater.inflate(R.layout.apptentive_survey_question_base, this);

		requiredView = findViewById(R.id.question_required);
		dashView = findViewById(R.id.question_dash);
		instructionsView = (TextView) findViewById(R.id.question_instructions);

		TextView title = (TextView) findViewById(R.id.question_title);
		title.setText(question.getValue());

		String instructionsText = question.getInstructions();
		setInstructions(instructionsText);

		validationFailedBorder = findViewById(R.id.validation_failed_border);
	}

	protected void setInstructions(String instructionsText) {
		boolean hasInstructions = !TextUtils.isEmpty(instructionsText);

		requiredView = findViewById(R.id.question_required);
		if (question.isRequired()) {
			requiredView.setVisibility(View.VISIBLE);
		} else {
			requiredView.setVisibility(View.GONE);
		}

		if (question.isRequired() && hasInstructions) {
			dashView.setVisibility(View.VISIBLE);
		} else {
			dashView.setVisibility(View.GONE);
		}

		if (hasInstructions) {
			instructionsView.setText(instructionsText);
			instructionsView.setVisibility(View.VISIBLE);
		} else {
			instructionsView.setVisibility(View.GONE);
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
			listener.onAnswered(this);
		}
	}

	/**
	 * Each subclass needs to override this based on the rules for that class.
	 * @return true if this question can be submitted, else false.
	 */
	public boolean isValid() {
		return false;
	}

	public void updateValidationState(boolean valid) {
		if (valid) {
			validationFailedBorder.setVisibility(View.INVISIBLE);
		} else {
			validationFailedBorder.setVisibility(View.VISIBLE);
		}
	}
}
