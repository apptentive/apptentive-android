/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.apptentive.android.sdk.module.engagement.interaction.view.survey.BaseSurveyQuestionView;
import com.apptentive.android.sdk.module.engagement.interaction.view.survey.MultichoiceSurveyQuestionView;
import com.apptentive.android.sdk.module.engagement.interaction.view.survey.MultiselectSurveyQuestionView;
import com.apptentive.android.sdk.module.engagement.interaction.view.survey.SurveyQuestionView;
import com.apptentive.android.sdk.module.engagement.interaction.view.survey.TextSurveyQuestionView;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;
import com.apptentive.android.sdk.module.survey.OnSurveyQuestionAnsweredListener;
import com.apptentive.android.sdk.storage.ApptentiveDatabase;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SurveyFragment extends ApptentiveBaseFragment<SurveyInteraction> implements OnSurveyQuestionAnsweredListener {

	private static final String EVENT_CANCEL = "cancel";
	private static final String EVENT_SUBMIT = "submit";
	private static final String EVENT_QUESTION_RESPONSE = "question_response";

	private LinearLayout questionsContainer;

	private Set<String> questionsWithSentMetrics;
	private Map<String, Object> answers;

	public static SurveyFragment newInstance(Bundle bundle) {
		SurveyFragment fragment = new SurveyFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (interaction == null) {
			getActivity().finish();
		}

		List<Question> questions = interaction.getQuestions();
		questionsWithSentMetrics = new HashSet<>(questions.size());
		answers = new LinkedHashMap<String, Object>(questions.size());

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

					// TODO: Extract survey state from views and send it as before.
					ApptentiveDatabase.getInstance(getActivity()).addPayload(new SurveyResponse(interaction, answers));

					Log.d("Survey Submitted.");
					callListener(true);
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
		if (savedInstanceState == null) {
			questionsContainer.removeAllViews();

			// Then render all the questions
			for (final Question question : questions) {
				final BaseSurveyQuestionView surveyQuestionView;
				if (question.getType() == Question.QUESTION_TYPE_SINGLELINE) {
					surveyQuestionView = TextSurveyQuestionView.newInstance((SinglelineQuestion) question);
				} else if (question.getType() == Question.QUESTION_TYPE_MULTICHOICE) {
					surveyQuestionView = MultichoiceSurveyQuestionView.newInstance((MultichoiceQuestion) question);

				} else if (question.getType() == Question.QUESTION_TYPE_MULTISELECT) {
					surveyQuestionView = MultiselectSurveyQuestionView.newInstance((MultiselectQuestion) question);
				} else {
					surveyQuestionView = null;
				}
				if (surveyQuestionView != null) {
					surveyQuestionView.setOnSurveyQuestionAnsweredListener(this);
					getRetainedChildFragmentManager().beginTransaction().add(R.id.questions, surveyQuestionView).commit();
				}
			}
		}
		return v;
	}

	/**
	 * Run this when the user hits the send button, and only send if it returns true. This method will update the visual validation state of all questions, and update the answers instance variable with the latest answer state.
	 *
	 * @return true if all questions that have constraints have met those constraints.
	 */
	public boolean validateAndUpdateState() {
		boolean validationPassed = true;

		List<Fragment> fragments = getRetainedChildFragmentManager().getFragments();
		for (Fragment fragment : fragments) {
			SurveyQuestionView surveyQuestionView = (SurveyQuestionView) fragment;
			boolean isValid = surveyQuestionView.isValid();
			surveyQuestionView.updateValidationState(isValid);
			if (!isValid) {
				validationPassed = false;
			}
		}
		return validationPassed;
	}

	void sendMetricForQuestion(Activity activity, String questionId) {
		JSONObject answerData = new JSONObject();
		try {
			answerData.put("id", questionId);
		} catch (JSONException e) {
			// Never happens.
		}
		EngagementModule.engageInternal(activity, interaction, EVENT_QUESTION_RESPONSE, answerData.toString());
		questionsWithSentMetrics.add(questionId);
	}

	private void callListener(boolean completed) {
		OnSurveyFinishedListener listener = ApptentiveInternal.getOnSurveyFinishedListener();
		if (listener != null) {
			listener.onSurveyFinished(completed);
		}
	}

	@Override
	public boolean onBackPressed() {
		EngagementModule.engageInternal(getActivity(), interaction, EVENT_CANCEL);
		return false;
	}

	@Override
	public void onAnswered(SurveyQuestionView surveyQuestionView) {
		String questionId = surveyQuestionView.getQuestionId();
		if (!questionsWithSentMetrics.contains(questionId)) {
			sendMetricForQuestion(getActivity(), questionId);
		}
		// Also clear validation state for questions that are no longer invalid.
		if (surveyQuestionView.isValid()) {
			surveyQuestionView.updateValidationState(true);
		}
	}
}