/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
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

import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.module.survey.*;
import com.apptentive.android.sdk.offline.PayloadManager;
import com.apptentive.android.sdk.offline.SurveyPayload;
import com.apptentive.android.sdk.util.Util;

import java.util.HashMap;
import java.util.Map;


/**
 * This module is responsible for fetching, displaying, and sending finished survey payloads to the apptentive server.
 *
 * @author Sky Kelsey
 */
public class SurveyModule {

	// *************************************************************************************************
	// ********************************************* Static ********************************************
	// *************************************************************************************************

	private static SurveyModule instance;

	static SurveyModule getInstance() {
		if (instance == null) {
			instance = new SurveyModule();
		}
		return instance;
	}


	// *************************************************************************************************
	// ********************************************* Private *******************************************
	// *************************************************************************************************

	private SurveyDefinition surveyDefinition;
	private SurveyPayload result;
	private SurveySendView sendView;
	private boolean fetching = false;
	private Map<String, String> data;

	private SurveyModule() {
		surveyDefinition = null;
	}

	private void setSurvey(SurveyDefinition surveyDefinition) {
		this.surveyDefinition = surveyDefinition;
		data = new HashMap<String, String>();
		data.put("id", surveyDefinition.getId());
	}


	// *************************************************************************************************
	// ******************************************* Not Private *****************************************
	// *************************************************************************************************

	/**
	 * Fetches a survey.
	 *
	 * @param onSurveyFetchedListener An optional {@link OnSurveyFetchedListener} that will be notified when the
	 *                                survey has been fetched. Pass in null if you don't need to be notified.
	 */
	public synchronized void fetchSurvey(final OnSurveyFetchedListener onSurveyFetchedListener) {
		if (fetching) {
			Log.d("Already fetching survey");
			return;
		}
		Log.d("Started survey fetch");
		fetching = true;
		// Upload any payloads that were created while the device was offline.
		new Thread() {
			public void run() {
				try {
					ApptentiveClient client = new ApptentiveClient(GlobalInfo.apiKey);
					SurveyDefinition definition = client.getSurvey();
					if (definition != null) {
						setSurvey(definition);
					}
					if (onSurveyFetchedListener != null) {
						onSurveyFetchedListener.onSurveyFetched(definition != null);
					}
				} finally {
					fetching = false;
				}
			}
		}.start();
	}

	public void show(Context context) {
		if (!isSurveyReady()) {
			return;
		}
		Intent intent = new Intent();
		intent.setClass(context, ApptentiveActivity.class);
		intent.putExtra("module", ApptentiveActivity.Module.SURVEY.toString());
		context.startActivity(intent);
	}

	public boolean isSurveyReady() {
		return (surveyDefinition != null);
	}

	public void cleanup() {
		this.surveyDefinition = null;
		this.result = null;
	}

	boolean isCompleted() {
		for (Question question : surveyDefinition.getQuestions()) {
			boolean required = question.isRequired();
			boolean answered = question.isAnswered();
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
		result = new SurveyPayload(surveyDefinition);

		TextView surveyTitle = (TextView) activity.findViewById(R.id.apptentive_survey_title_text);
		surveyTitle.setFocusable(true);
		surveyTitle.setFocusableInTouchMode(true);
		surveyTitle.setText(surveyDefinition.getName());

		Button skipButton = (Button) activity.findViewById(R.id.apptentive_survey_button_skip);
		if (surveyDefinition.isRequired()) {
			((RelativeLayout) skipButton.getParent()).removeView(skipButton);
		} else {
			skipButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					MetricModule.sendMetric(MetricModule.Event.survey__cancel, null, data);
					cleanup();
					activity.finish();
				}
			});
		}

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
			final int index = surveyDefinition.getQuestions().indexOf(question);
			if (question.getType() == Question.QUESTION_TYPE_SINGLELINE) {
				TextSurveyQuestionView textQuestionView = new TextSurveyQuestionView(activity, (SinglelineQuestion) question);
				textQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener<TextSurveyQuestionView>() {
					public void onAnswered(TextSurveyQuestionView view) {
						sendMetricForQuestion(question);
						sendView.setEnabled(isCompleted());
					}
				});
				questionList.addView(textQuestionView);
			} else if (question.getType() == Question.QUESTION_TYPE_MULTICHOICE) {
				MultichoiceSurveyQuestionView multichoiceQuestionView = new MultichoiceSurveyQuestionView(activity, (MultichoiceQuestion) question);
				multichoiceQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener<MultichoiceSurveyQuestionView>() {
					public void onAnswered(MultichoiceSurveyQuestionView view) {
						sendMetricForQuestion(question);
						sendView.setEnabled(isCompleted());
					}
				});
				questionList.addView(multichoiceQuestionView);
			} else if (question.getType() == Question.QUESTION_TYPE_MULTISELECT) {
				MultiselectSurveyQuestionView multiselectQuestionView = new MultiselectSurveyQuestionView(activity, (MultiselectQuestion) question);
				multiselectQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener<MultiselectSurveyQuestionView>() {
					public void onAnswered(MultiselectSurveyQuestionView view) {
						sendMetricForQuestion(question);
						sendView.setEnabled(isCompleted());
					}
				});
				questionList.addView(multiselectQuestionView);
			} else if (question.getType() == Question.QUESTION_TYPE_STACKRANK) {
				StackrankSurveyQuestionView questionView = new StackrankSurveyQuestionView(activity, (StackrankQuestion) question);
				questionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener() {
					public void onAnswered(Object view) {
						sendMetricForQuestion(question);
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
				MetricModule.sendMetric(MetricModule.Event.survey__submit, null, data);
				PayloadManager.getInstance().putPayload(result);
				if (surveyDefinition.isShowSuccessMessage() && surveyDefinition.getSuccessMessage() != null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
					builder.setMessage(surveyDefinition.getSuccessMessage());
					builder.setTitle("Survey Completed");
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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

		MetricModule.sendMetric(MetricModule.Event.survey__launch, null, data);

		// Force the top of the survey to be shown first.
		surveyTitle.requestFocus();
	}

	void sendMetricForQuestion(Question question) {
		if(!question.isMetricSent() && question.isAnswered()) {
			Map<String, String> answerData = new HashMap<String, String>();
			answerData.put("id", question.getId());
			answerData.put("survey_id", surveyDefinition.getId());
			MetricModule.sendMetric(MetricModule.Event.survey__question_response, null, answerData);
			question.setMetricSent(true);
		}

	}
}
