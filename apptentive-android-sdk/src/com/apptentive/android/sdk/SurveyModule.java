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

import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.module.survey.*;
import com.apptentive.android.sdk.offline.SurveyPayload;
import com.apptentive.android.sdk.storage.PayloadStore;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;

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
	private SurveySendView sendView;
	private boolean fetching = false;
	private Map<String, String> data;
	private OnSurveyFinishedListener onSurveyFinishedListener;

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

		new Thread() {
			public void run() {
				try {
					ApptentiveHttpResponse response = ApptentiveClient.getSurvey();
					if(response.isSuccessful()) {
						SurveyDefinition definition = SurveyManager.parseSurvey(response.getContent());
						if (definition != null) {
							setSurvey(definition);
						}
						if (onSurveyFetchedListener != null) {
							onSurveyFetchedListener.onSurveyFetched(definition != null);
						}
					}
				} catch (JSONException e) {
					Log.e("Exception parsing survey JSON.", e);
				} finally {
					fetching = false;
				}
			}
		}.start();
	}

	public void show(Context context) {
		show(context, null);
	}

	public void show(Context context, OnSurveyFinishedListener onSurveyFinishedListener) {
		if (!isSurveyReady()) {
			return;
		}
		this.onSurveyFinishedListener = onSurveyFinishedListener;
		Intent intent = new Intent();
		intent.setClass(context, ViewActivity.class);
		intent.putExtra("module", ViewActivity.Module.SURVEY.toString());
		context.startActivity(intent);
	}

	/**
	 * A method for querying whether a survey is downloaded and ready to show to the user.
	 * @return true if a survey is ready, else false.
	 */
	public boolean isSurveyReady() {
		return (surveyDefinition != null);
	}

	public void cleanup() {
		this.surveyDefinition = null;
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
					onBackPressed(activity);
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

				getSurveyStore(activity).addPayload(new SurveyPayload(surveyDefinition));

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

		// Force the top of the survey to be shown first.
		surveyTitle.requestFocus();
	}

	void sendMetricForQuestion(Context context, Question question) {
		if(!question.isMetricSent() && question.isAnswered()) {
			Map<String, String> answerData = new HashMap<String, String>();
			answerData.put("id", question.getId());
			answerData.put("survey_id", surveyDefinition.getId());
			MetricModule.sendMetric(context, Event.EventLabel.survey__question_response, null, answerData);
			question.setMetricSent(true);
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
