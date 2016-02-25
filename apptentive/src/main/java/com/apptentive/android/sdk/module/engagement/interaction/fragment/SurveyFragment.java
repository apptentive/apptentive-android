/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apptentive.android.sdk.ApptentiveInternal;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.SurveyResponse;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.SurveyInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.MultichoiceQuestion;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.MultiselectQuestion;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.Question;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.SinglelineQuestion;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.SurveyState;
import com.apptentive.android.sdk.module.engagement.interaction.view.survey.BaseSurveyQuestionView;
import com.apptentive.android.sdk.module.engagement.interaction.view.survey.MultichoiceSurveyQuestionView;
import com.apptentive.android.sdk.module.engagement.interaction.view.survey.MultiselectSurveyQuestionView;
import com.apptentive.android.sdk.module.engagement.interaction.view.survey.TextSurveyQuestionView;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;
import com.apptentive.android.sdk.module.survey.OnSurveyQuestionAnsweredListener;
import com.apptentive.android.sdk.storage.ApptentiveDatabase;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class SurveyFragment extends ApptentiveBaseFragment<SurveyInteraction> {

	private static final String EVENT_CANCEL = "cancel";
	private static final String EVENT_SUBMIT = "submit";
	private static final String EVENT_QUESTION_RESPONSE = "question_response";

	private static final String KEY_SURVEY_SUBMITTED = "survey_submitted";
	private static final String KEY_SURVEY_DATA = "survey_data";
	private boolean surveySubmitted = false;

	private SurveyState surveyState;

	private LinearLayout questionsContainer;

	public static SurveyFragment newInstance(Bundle bundle) {
		SurveyFragment fragment = new SurveyFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			surveySubmitted = savedInstanceState.getBoolean(KEY_SURVEY_SUBMITTED, false);
			surveyState = savedInstanceState.getParcelable(KEY_SURVEY_DATA);
		}
		if (surveyState == null) {
			surveyState = new SurveyState(interaction);
		}
		if (interaction == null || surveySubmitted) {
			getActivity().finish();
		}

		// create ContextThemeWrapper from the original Activity Context with the apptentive theme
		final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), ApptentiveInternal.apptentiveTheme);
		// clone the inflater using the ContextThemeWrapper
		final LayoutInflater themedInflater = inflater.cloneInContext(contextThemeWrapper);
		View v = themedInflater.inflate(R.layout.apptentive_survey, container, false);

		TextView description = (TextView) v.findViewById(R.id.description);
		description.setText(interaction.getDescription());

		final Button send = (Button) v.findViewById(R.id.send);
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Util.hideSoftKeyboard(getActivity(), view);
				boolean valid = validateAndUpdateState();
				if (valid) {
					surveySubmitted = true;
					if (interaction.isShowSuccessMessage() && !TextUtils.isEmpty(interaction.getSuccessMessage())) {
						Toast toast = new Toast(contextThemeWrapper);
						toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);
						toast.setDuration(Toast.LENGTH_SHORT);
						View toastView = themedInflater.inflate(R.layout.apptentive_survey_sent_toast, (LinearLayout) getView().findViewById(R.id.survey_sent_toast_root));
						toast.setView(toastView);
						((TextView) toastView.findViewById(R.id.survey_sent_toast_text)).setText(interaction.getSuccessMessage());

						toast.show();
					}
					getActivity().finish();

					EngagementModule.engageInternal(getActivity(), interaction, EVENT_SUBMIT);
					ApptentiveDatabase.getInstance(getActivity()).addPayload(new SurveyResponse(interaction, surveyState));
					Log.d("Survey Submitted.");
					callListener(true);
					cleanup();
				} else {
					Toast toast = new Toast(contextThemeWrapper);
					toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);
					toast.setDuration(Toast.LENGTH_SHORT);
					View toastView = themedInflater.inflate(R.layout.apptentive_survey_invalid_toast, (LinearLayout) getView().findViewById(R.id.survey_invalid_toast_root));
					toast.setView(toastView);
					((TextView) toastView.findViewById(R.id.survey_invalid_toast_text)).setText("Missing Required Question");

					toast.show();
				}
			}
		});

		questionsContainer = (LinearLayout) v.findViewById(R.id.questions);
		questionsContainer.removeAllViews();

		// Then render all the questions
		for (final Question question : interaction.getQuestions()) {
			final BaseSurveyQuestionView surveyQuestionView;
			if (question.getType() == Question.QUESTION_TYPE_SINGLELINE) {
				surveyQuestionView = new TextSurveyQuestionView(getActivity(), surveyState, (SinglelineQuestion) question);
			} else if (question.getType() == Question.QUESTION_TYPE_MULTICHOICE) {
				surveyQuestionView = new MultichoiceSurveyQuestionView(getActivity(), surveyState, (MultichoiceQuestion) question);
			} else if (question.getType() == Question.QUESTION_TYPE_MULTISELECT) {
				surveyQuestionView = new MultiselectSurveyQuestionView(getActivity(), surveyState, (MultiselectQuestion) question);
			} else {
				surveyQuestionView = null;
			}
			if (surveyQuestionView != null) {
				surveyQuestionView.setTag(R.id.apptentive_survey_question_id, question.getId());
				surveyQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener() {
					public void onAnswered() {
						sendMetricForQuestion(getActivity(), question);
						// Also clear validation state for questions that are no longer invalid.
						if (surveyState.isQuestionValid(question)) {
							surveyQuestionView.updateValidationState();
						}
					}
				});
				questionsContainer.addView(surveyQuestionView);
			}
		}
		return v;
	}

	/**
	 * Run this when the user hits the send button, and only send if it returns true. This method will update the visual validation state of all questions.
	 * @return true if all questions that have constraints have met those constraints.
	 */
	public boolean validateAndUpdateState() {
		boolean validationPassed = true;
		Map<String, Question> questionsMap = interaction.getQuestionsAsMap();
		if (questionsContainer != null) {
			for (int i = 0; i < questionsContainer.getChildCount(); i++) {
				BaseSurveyQuestionView surveyQuestionView = (BaseSurveyQuestionView) questionsContainer.getChildAt(i);
				String questionId = (String) surveyQuestionView.getTag(R.id.apptentive_survey_question_id);
				if (!surveyState.isQuestionValid(questionsMap.get(questionId))) {
					validationPassed = false;
				}
				surveyQuestionView.updateValidationState();
			}
		}
		return validationPassed;
	}

	void sendMetricForQuestion(Activity activity, Question question) {
		String questionId = question.getId();
		if (!surveyState.isMetricSent(questionId) && surveyState.isQuestionValid(question)) {
			JSONObject answerData = new JSONObject();
			try {
				answerData.put("id", question.getId());
			} catch (JSONException e) {
				// Never happens.
			}
			EngagementModule.engageInternal(activity, interaction, EVENT_QUESTION_RESPONSE, answerData.toString());
			surveyState.markMetricSent(questionId);
		}
	}

	private void cleanup() {
		surveyState = null;
	}

	private void callListener(boolean completed) {
		OnSurveyFinishedListener listener = ApptentiveInternal.getOnSurveyFinishedListener();
		if (listener != null) {
			listener.onSurveyFinished(completed);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_SURVEY_SUBMITTED, surveySubmitted);
		outState.putParcelable(KEY_SURVEY_DATA, surveyState);
	}

	@Override
	public boolean onBackPressed() {
		EngagementModule.engageInternal(getActivity(), interaction, EVENT_CANCEL);
		return false;
	}
}