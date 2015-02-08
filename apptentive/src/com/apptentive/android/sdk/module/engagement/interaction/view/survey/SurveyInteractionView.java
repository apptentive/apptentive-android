/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.model.SurveyResponse;
import com.apptentive.android.sdk.module.engagement.interaction.model.SurveyInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.*;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.view.InteractionView;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;
import com.apptentive.android.sdk.module.survey.OnSurveyQuestionAnsweredListener;
import com.apptentive.android.sdk.storage.ApptentiveDatabase;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Sky Kelsey
 */
public class SurveyInteractionView extends InteractionView<SurveyInteraction> {

	private static final String EVENT_CANCEL = "cancel";
	private static final String EVENT_SUBMIT = "submit";
	private static final String EVENT_QUESTION_RESPONSE = "question_response";

	private static final String KEY_SURVEY_SUBMITTED = "survey_submitted";
	private boolean surveySubmitted = false;

	private static SurveyState surveyState;
	private static JSONObject data;

	public SurveyInteractionView(SurveyInteraction interaction) {
		super(interaction);
		if (surveyState == null) {
			surveyState = new SurveyState(interaction);
		}
		if (data == null) {
			data = new JSONObject();
			try {
				data.put("id", interaction.getId());
			} catch (JSONException e) {
				// Never happens.
			}
		}
	}

	@Override
	public void doOnCreate(final Activity activity, Bundle savedInstanceState) {

		if (savedInstanceState != null) {
			surveySubmitted = savedInstanceState.getBoolean(KEY_SURVEY_SUBMITTED, false);
		}

		if (interaction == null || surveySubmitted) {
			activity.finish();
			return;
		}

		activity.setContentView(R.layout.apptentive_survey);

		// Hide branding if needed.
		final View branding = activity.findViewById(R.id.apptentive_branding_view);
		if (branding != null) {
			if (Configuration.load(activity).isHideBranding(activity)) {
				branding.setVisibility(View.GONE);
			}
		}

		TextView title = (TextView) activity.findViewById(R.id.title);
		title.setFocusable(true);
		title.setFocusableInTouchMode(true);
		title.setText(interaction.getName());

		String descriptionText = interaction.getDescription();
		if (descriptionText != null) {
			TextView description = (TextView) activity.findViewById(R.id.description);
			description.setText(descriptionText);
			description.setVisibility(View.VISIBLE);
		}

		final Button send = (Button) activity.findViewById(R.id.send);
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Util.hideSoftKeyboard(activity, view);
				surveySubmitted = true;
				if (interaction.isShowSuccessMessage() && interaction.getSuccessMessage() != null) {
					SurveyThankYouDialog dialog = new SurveyThankYouDialog(activity);
					dialog.setMessage(interaction.getSuccessMessage());
					dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialogInterface) {
							activity.finish();
						}
					});
					dialog.show();
				} else {
					activity.finish();
				}

				EngagementModule.engageInternal(activity, interaction, EVENT_SUBMIT, data.toString());
				ApptentiveDatabase.getInstance(activity).addPayload(new SurveyResponse(interaction, surveyState));
				Log.d("Survey Submitted.");
				callListener(true);

				cleanup();
			}
		});

		LinearLayout questions = (LinearLayout) activity.findViewById(R.id.questions);
		questions.removeAllViews();

		// Then render all the questions
		for (final Question question : interaction.getQuestions()) {
			if (question.getType() == Question.QUESTION_TYPE_SINGLELINE) {
				TextSurveyQuestionView textQuestionView = new TextSurveyQuestionView(activity, surveyState, (SinglelineQuestion) question);
				textQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener() {
					public void onAnswered() {
						sendMetricForQuestion(activity, question);
						send.setEnabled(isSurveyValid());
					}
				});
				questions.addView(textQuestionView);
			} else if (question.getType() == Question.QUESTION_TYPE_MULTICHOICE) {
				MultichoiceSurveyQuestionView multichoiceQuestionView = new MultichoiceSurveyQuestionView(activity, surveyState, (MultichoiceQuestion) question);
				multichoiceQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener() {
					public void onAnswered() {
						sendMetricForQuestion(activity, question);
						send.setEnabled(isSurveyValid());
					}
				});
				questions.addView(multichoiceQuestionView);
			} else if (question.getType() == Question.QUESTION_TYPE_MULTISELECT) {
				MultiselectSurveyQuestionView multiselectQuestionView = new MultiselectSurveyQuestionView(activity, surveyState, (MultiselectQuestion) question);
				multiselectQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener() {
					public void onAnswered() {
						sendMetricForQuestion(activity, question);
						send.setEnabled(isSurveyValid());
					}
				});
				questions.addView(multiselectQuestionView);
			}
		}

		send.setEnabled(isSurveyValid());

		// Force the top of the survey to be shown first.
		title.requestFocus();
	}

	public boolean isSurveyValid() {
		for (Question question : interaction.getQuestions()) {
			if (!surveyState.isQuestionValid(question)) {
				return false;
			}
		}
		return true;
	}

	void sendMetricForQuestion(Activity activity, Question question) {
		String questionId = question.getId();
		if (!surveyState.isMetricSent(questionId) && surveyState.isQuestionValid(question)) {
			String answerData = String.format("{\"id\":\"%s\",\"survey_id\":\"%s\"}", question.getId(), interaction.getId());
			EngagementModule.engageInternal(activity, interaction, EVENT_QUESTION_RESPONSE, answerData);
			surveyState.markMetricSent(questionId);
		}
	}

	private void cleanup() {
		surveyState = null;
		data = null;
	}


	@Override
	public boolean onBackPressed(Activity activity) {
		// If this survey is required, do not let it be dismissed when the user clicks the back button.
		if (!interaction.isRequired()) {
			EngagementModule.engageInternal(activity, interaction, EVENT_CANCEL, data.toString());
			callListener(false);
			cleanup();
			return true;
		} else {
			return false;
		}
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
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		surveySubmitted = savedInstanceState.getBoolean(KEY_SURVEY_SUBMITTED, false);
	}
}
