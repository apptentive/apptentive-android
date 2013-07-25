/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.*;

import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.model.SurveyResponse;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.module.survey.*;
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
	private SurveySendView sendView;
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

	public void show(Context context, SurveyDefinition surveyDefinition, OnSurveyFinishedListener onSurveyFinishedListener) {
		this.surveyDefinition = surveyDefinition;
		this.surveyState = new SurveyState(surveyDefinition);
		this.onSurveyFinishedListener = onSurveyFinishedListener;
		data = new HashMap<String, String>();
		data.put("id", surveyDefinition.getId());

		Intent intent = new Intent();
		intent.setClass(context, ViewActivity.class);
		intent.putExtra("module", ViewActivity.Module.SURVEY.toString());
		context.startActivity(intent);
	}

	public SurveyState getSurveyState() {
		return this.surveyState;
	}

	boolean isCompleted() {
		for (Question question : surveyDefinition.getQuestions()) {
			String questionId = question.getId();
			boolean required = question.isRequired();
			boolean answered = surveyState.isAnswered(questionId);
			if (required && !answered) {
				return false;
			}
		}
		return true;
	}

	void doShow(final Activity activity) {
		if (surveyDefinition == null) {
			return;
		}
		TextView surveyTitle = (TextView) activity.findViewById(R.id.apptentive_survey_title_text);
		surveyTitle.setFocusable(true);
		surveyTitle.setFocusableInTouchMode(true);
		surveyTitle.setText(surveyDefinition.getName());

/*
		// TODO: Put this into the onBackButtonPressed method.
		Button skipButton = (Button) activity.findViewById(R.id.apptentive_survey_button_skip);
		if (surveyDefinition.isRequired()) {
			((RelativeLayout) skipButton.getParent()).removeView(skipButton);
		} else {
			skipButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					onBackPressed(activity);
					activity.finish();
				}
			});
		}
*/

		View brandingButton = activity.findViewById(R.id.apptentive_branding_view);
		brandingButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				AboutModule.getInstance().show(activity);
			}
		});

		LinearLayout questionList = (LinearLayout) activity.findViewById(R.id.aptentive_survey_question_list);

		// Render the survey description
		if (surveyDefinition.getDescription() != null) {
			SurveyDescriptionView surveyDescription = new SurveyDescriptionView(activity);
			surveyDescription.setTitleText(surveyDefinition.getDescription());
			questionList.addView(surveyDescription);
		}

		// Then render all the questions
		for (final Question question : surveyDefinition.getQuestions()) {
			if (question.getType() == Question.QUESTION_TYPE_SINGLELINE) {
				TextSurveyQuestionView textQuestionView = new TextSurveyQuestionView(activity, (SinglelineQuestion) question);
				textQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener<TextSurveyQuestionView>() {
					public void onAnswered(TextSurveyQuestionView view) {
						sendMetricForQuestion(activity, question);
						sendView.setEnabled(isCompleted());
					}
				});
				questionList.addView(textQuestionView);
			} else if (question.getType() == Question.QUESTION_TYPE_MULTICHOICE) {
				MultichoiceSurveyQuestionView multichoiceQuestionView = new MultichoiceSurveyQuestionView(activity, (MultichoiceQuestion) question);
				multichoiceQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener<MultichoiceSurveyQuestionView>() {
					public void onAnswered(MultichoiceSurveyQuestionView view) {
						sendMetricForQuestion(activity, question);
						sendView.setEnabled(isCompleted());
					}
				});
				questionList.addView(multichoiceQuestionView);
			} else if (question.getType() == Question.QUESTION_TYPE_MULTISELECT) {
				MultiselectSurveyQuestionView multiselectQuestionView = new MultiselectSurveyQuestionView(activity, (MultiselectQuestion) question);
				multiselectQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener<MultiselectSurveyQuestionView>() {
					public void onAnswered(MultiselectSurveyQuestionView view) {
						sendMetricForQuestion(activity, question);
						sendView.setEnabled(isCompleted());
					}
				});
				questionList.addView(multiselectQuestionView);
			} else if (question.getType() == Question.QUESTION_TYPE_STACKRANK) {
				StackrankSurveyQuestionView questionView = new StackrankSurveyQuestionView(activity, (StackrankQuestion) question);
				questionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener() {
					public void onAnswered(Object view) {
						sendMetricForQuestion(activity, question);
						// TODO: This.
					}
				});
				questionList.addView(questionView);
			}
		}

		// Then render the send button.
		sendView = new SurveySendView(activity);
		sendView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Util.hideSoftKeyboard(activity, view);
				MetricModule.sendMetric(activity, Event.EventLabel.survey__submit, null, data);

				getSurveyStore(activity).addPayload(new SurveyResponse(surveyDefinition));

				if(SurveyModule.this.onSurveyFinishedListener != null) {
					SurveyModule.this.onSurveyFinishedListener.onSurveyFinished(true);
				}

				if (surveyDefinition.isShowSuccessMessage() && surveyDefinition.getSuccessMessage() != null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
					builder.setMessage(surveyDefinition.getSuccessMessage());
					builder.setTitle(view.getContext().getString(R.string.apptentive_thanks));
					builder.setPositiveButton(view.getContext().getString(R.string.apptentive_ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogInterface, int i) {
							cleanup();
							activity.finish();
						}
					});
					builder.show();
				} else {
					cleanup();
					activity.finish();
				}
			}
		});
		sendView.setEnabled(isCompleted());
		questionList.addView(sendView);

		MetricModule.sendMetric(activity, Event.EventLabel.survey__launch, null, data);
		SurveyHistory.recordSurveyDisplay(activity, surveyDefinition.getId(), System.currentTimeMillis());

		// Force the top of the survey to be shown first.
		surveyTitle.requestFocus();
	}

	void sendMetricForQuestion(Context context, Question question) {
		String questionId = question.getId();
		if(!surveyState.isMetricSent(questionId) && surveyState.isAnswered(questionId)) {
			Map<String, String> answerData = new HashMap<String, String>();
			answerData.put("id", question.getId());
			answerData.put("survey_id", surveyDefinition.getId());
			MetricModule.sendMetric(context, Event.EventLabel.survey__question_response, null, answerData);
			surveyState.markMetricSent(questionId);
		}
	}

	private static PayloadStore getSurveyStore(Context context) {
		return Apptentive.getDatabase(context);
	}

	void onBackPressed(Context context) {
		MetricModule.sendMetric(context, Event.EventLabel.survey__cancel, null, data);
		if(SurveyModule.this.onSurveyFinishedListener != null) {
			SurveyModule.this.onSurveyFinishedListener.onSurveyFinished(false);
		}
		cleanup();
	}
}
