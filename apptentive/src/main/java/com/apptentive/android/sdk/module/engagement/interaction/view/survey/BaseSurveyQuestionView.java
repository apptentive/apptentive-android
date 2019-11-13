/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.survey.OnSurveyQuestionAnsweredListener;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.Question;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;


abstract public class BaseSurveyQuestionView<Q extends Question> extends Fragment implements SurveyQuestionView {

	private static final String SENT_METRIC = "sent_metric";

	protected Q question;

	private FrameLayout root;
	private TextView requiredView;
	private View dashView;
	private TextView instructionsView;
	private View validationFailedBorder;

	private boolean sentMetric;
	private OnSurveyQuestionAnsweredListener listener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.apptentive_survey_question_base, container, false);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		try {
			root = (FrameLayout) view.findViewById(R.id.question_base);
			requiredView = (TextView) view.findViewById(R.id.question_required);
			dashView = view.findViewById(R.id.dash_view);
			instructionsView = (TextView) view.findViewById(R.id.question_instructions);

			// Makes UI tests easier. We can potentially obviate this if surveys used a RecyclerView.
			root.setTag(Integer.parseInt(getTag()));

			TextView title = (TextView) view.findViewById(R.id.question_title);
			title.setText(question.getValue());
			if (question.isRequired()) {
				title.setContentDescription(question.getValue() + ". " + getString(R.string.apptentive_required));
			}

			setInstructions(question.getRequiredText(), question.getInstructions());

			validationFailedBorder = view.findViewById(R.id.validation_failed_border);

			sentMetric = (savedInstanceState != null) && savedInstanceState.getBoolean(SENT_METRIC, false);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception in %s.onCreateView()", BaseSurveyQuestionView.class.getSimpleName());
			logException(e);
		}
	}


	protected void setInstructions(String requiredText, String instructionsText) {
		boolean showRequiredText = question.isRequired();
		boolean showInstructions = !TextUtils.isEmpty(instructionsText);

		if (showRequiredText) {
			if (TextUtils.isEmpty(requiredText)) {
				requiredText = "Required";
			}
			requiredView.setText(requiredText);
			requiredView.setVisibility(View.VISIBLE);
		} else {
			requiredView.setVisibility(View.GONE);
		}

		if (showInstructions) {
			instructionsView.setText(instructionsText);
			instructionsView.setVisibility(View.VISIBLE);
		} else {
			instructionsView.setVisibility(View.GONE);
		}

		if (showRequiredText && showInstructions) {
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

	@Override
	public String getErrorMessage() {
		return question.getErrorMessage();
	}

	@Override
	public boolean didSendMetric() {
		return sentMetric;
	}

	@Override
	public void setSentMetric(boolean sent) {
		sentMetric = sent;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(SENT_METRIC, sentMetric);
	}
}
