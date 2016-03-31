/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.survey.OnSurveyQuestionAnsweredListener;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.Question;


abstract public class BaseSurveyQuestionView<Q extends Question> extends Fragment implements SurveyQuestionView {

	protected Q question;
	private OnSurveyQuestionAnsweredListener listener;

	protected View requiredView;
	protected View dashView;
	protected TextView instructionsView;

	private View validationFailedBorder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
													 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		Context contextThemeWrapper = new ContextThemeWrapper(getContext(), ApptentiveInternal.getInstance().getApptentiveTheme());
		LayoutInflater themedInflater = LayoutInflater.from(contextThemeWrapper);

		View v = themedInflater.inflate(R.layout.apptentive_survey_question_base, container, false);
		return v;
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		requiredView = view.findViewById(R.id.question_required);
		dashView = view.findViewById(R.id.dash_view);
		instructionsView = (TextView) view.findViewById(R.id.question_instructions);

		TextView title = (TextView) view.findViewById(R.id.question_title);
		title.setText(question.getValue());

		String instructionsText = question.getInstructions();
		setInstructions(view, instructionsText);

		validationFailedBorder = view.findViewById(R.id.validation_failed_border);
	}


	protected void setInstructions(final View v, String instructionsText) {
		boolean hasInstructions = !TextUtils.isEmpty(instructionsText);

		requiredView = v.findViewById(R.id.question_required);
		if (question.isRequired()) {
			requiredView.setVisibility(View.VISIBLE);
		} else {
			requiredView.setVisibility(View.GONE);
		}

		if (hasInstructions) {
			instructionsView.setText(instructionsText);
			instructionsView.setVisibility(View.VISIBLE);
		} else {
			instructionsView.setVisibility(View.GONE);
		}

		if(question.isRequired() && hasInstructions) {
			dashView.setVisibility(View.VISIBLE);
		} else {
			dashView.setVisibility(View.GONE);
		}
	}

	protected LinearLayout getAnswerContainer(View rootView) {
		return (LinearLayout) rootView.findViewById(R.id.answer_container);
	}

	/**
	 * Always call this when the answer value changes.
	 */
	protected void fireListener() {
		if (listener != null) {
			listener.onAnswered(this);
		}
	}

	public void updateValidationState(boolean valid) {
		if (valid) {
			validationFailedBorder.setVisibility(View.INVISIBLE);
		} else {
			validationFailedBorder.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void setOnSurveyQuestionAnsweredListener(OnSurveyQuestionAnsweredListener listener) {
		this.listener = listener;
	}

	@Override
	public String getQuestionId() {
		return question.getId();
	}

	public abstract boolean isValid();

	public abstract Object getAnswer();
}
