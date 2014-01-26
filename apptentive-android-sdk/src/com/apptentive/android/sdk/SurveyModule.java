/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.*;

import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.model.SurveyResponse;
import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.module.survey.*;
import com.apptentive.android.sdk.module.survey.view.MultichoiceSurveyQuestionView;
import com.apptentive.android.sdk.module.survey.view.MultiselectSurveyQuestionView;
import com.apptentive.android.sdk.module.survey.view.SurveyThankYouDialog;
import com.apptentive.android.sdk.module.survey.view.TextSurveyQuestionView;
import com.apptentive.android.sdk.storage.ApptentiveDatabase;
import com.apptentive.android.sdk.storage.PayloadStore;
import com.apptentive.android.sdk.util.Util;

import java.util.HashMap;
import java.util.Map;


/**
 * This module is responsible for displaying Surveys.
 *
 * @author Sky Kelsey
 */
public class SurveyModule {

	// *************************************************************************************************
	// ********************************************* Static ********************************************
	// *************************************************************************************************

	private static SurveyModule instance;

	public static SurveyModule getInstance() {
		if (instance == null) {
			instance = new SurveyModule();
		}
		return instance;
	}


	// *************************************************************************************************
	// ********************************************* Private *******************************************
	// *************************************************************************************************

	private SurveyDefinition surveyDefinition;
	private SurveyState surveyState;
	private Map<String, String> data;
	private OnSurveyFinishedListener onSurveyFinishedListener;

	private SurveyModule() {
	}

	private void cleanup() {
		this.surveyDefinition = null;
		this.surveyState = null;
		this.onSurveyFinishedListener = null;
		this.data = null;
	}

	// *************************************************************************************************
	// ******************************************* Not Private *****************************************
	// *************************************************************************************************

	public void show(Activity activity, SurveyDefinition surveyDefinition, OnSurveyFinishedListener onSurveyFinishedListener) {
		this.surveyDefinition = surveyDefinition;
		this.surveyState = new SurveyState(surveyDefinition);
		this.onSurveyFinishedListener = onSurveyFinishedListener;
		data = new HashMap<String, String>();
		data.put("id", surveyDefinition.getId());

		Intent intent = new Intent();
		intent.setClass(activity, ViewActivity.class);
		intent.putExtra(ActivityContent.KEY, ActivityContent.Type.SURVEY.toString());
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_up_in, R.anim.slide_down_out);
	}

	public SurveyState getSurveyState() {
		return this.surveyState;
	}

	public boolean isSurveyValid() {
		for (Question question : surveyDefinition.getQuestions()) {
			if (!surveyState.isQuestionValid(question)) {
				return false;
			}
		}
		return true;
	}

	void doShow(final Activity activity) {
		if (surveyDefinition == null) {
			activity.finish();
			return;
		}

		activity.setContentView(R.layout.apptentive_survey);

		TextView title = (TextView) activity.findViewById(R.id.title);
		title.setFocusable(true);
		title.setFocusableInTouchMode(true);
		title.setText(surveyDefinition.getName());

		String descriptionText = surveyDefinition.getDescription();
		TextView description = (TextView) activity.findViewById(R.id.description);
		if (descriptionText != null) {
			description.setText(descriptionText);
		} else {
			description.setVisibility(View.GONE);
		}

		final Button send = (Button) activity.findViewById(R.id.send);
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Util.hideSoftKeyboard(activity, view);

				if (surveyDefinition.isShowSuccessMessage() && surveyDefinition.getSuccessMessage() != null) {
					SurveyThankYouDialog dialog = new SurveyThankYouDialog(activity);
					dialog.setMessage(surveyDefinition.getSuccessMessage());
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

				MetricModule.sendMetric(activity, Event.EventLabel.survey__submit, null, data);
				getSurveyStore(activity).addPayload(new SurveyResponse(surveyDefinition));
				Log.d("Survey Submitted.");

				if (SurveyModule.this.onSurveyFinishedListener != null) {
					SurveyModule.this.onSurveyFinishedListener.onSurveyFinished(true);
				}
				cleanup();
			}
		});

		LinearLayout questions = (LinearLayout) activity.findViewById(R.id.questions);

		// Then render all the questions
		for (final Question question : surveyDefinition.getQuestions()) {
			if (question.getType() == Question.QUESTION_TYPE_SINGLELINE) {
				TextSurveyQuestionView textQuestionView = new TextSurveyQuestionView(activity, (SinglelineQuestion) question);
				textQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener() {
					public void onAnswered() {
						sendMetricForQuestion(activity, question);
						send.setEnabled(isSurveyValid());
					}
				});
				questions.addView(textQuestionView);
			} else if (question.getType() == Question.QUESTION_TYPE_MULTICHOICE) {
				MultichoiceSurveyQuestionView multichoiceQuestionView = new MultichoiceSurveyQuestionView(activity, (MultichoiceQuestion) question);
				multichoiceQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener() {
					public void onAnswered() {
						sendMetricForQuestion(activity, question);
						send.setEnabled(isSurveyValid());
					}
				});
				questions.addView(multichoiceQuestionView);
			} else if (question.getType() == Question.QUESTION_TYPE_MULTISELECT) {
				MultiselectSurveyQuestionView multiselectQuestionView = new MultiselectSurveyQuestionView(activity, (MultiselectQuestion) question);
				multiselectQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener() {
					public void onAnswered() {
						sendMetricForQuestion(activity, question);
						send.setEnabled(isSurveyValid());
					}
				});
				questions.addView(multiselectQuestionView);
//		} else if (question.getType() == Question.QUESTION_TYPE_STACKRANK) {
//			// TODO: This.
			}
		}
		MetricModule.sendMetric(activity, Event.EventLabel.survey__launch, null, data);

		send.setEnabled(isSurveyValid());

		// Force the top of the survey to be shown first.
		title.requestFocus();
	}

	void sendMetricForQuestion(Context context, Question question) {
		String questionId = question.getId();
		if (!surveyState.isMetricSent(questionId) && surveyState.isQuestionValid(question)) {
			Map<String, String> answerData = new HashMap<String, String>();
			answerData.put("id", question.getId());
			answerData.put("survey_id", surveyDefinition.getId());
			MetricModule.sendMetric(context, Event.EventLabel.survey__question_response, null, answerData);
			surveyState.markMetricSent(questionId);
		}
	}

	private static PayloadStore getSurveyStore(Context context) {
		return ApptentiveDatabase.getInstance(context);
	}

	void onBackPressed(Activity activity) {
		MetricModule.sendMetric(activity, Event.EventLabel.survey__cancel, null, data);
		if (SurveyModule.this.onSurveyFinishedListener != null) {
			SurveyModule.this.onSurveyFinishedListener.onSurveyFinished(false);
		}
		cleanup();
		activity.finish();
		activity.overridePendingTransition(R.anim.slide_up_in, R.anim.slide_down_out);
	}
}
