/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.SurveyModule;
import com.apptentive.android.sdk.module.rating.view.ApptentiveBaseDialog;
import com.apptentive.android.sdk.module.survey.*;

/**
 * @author Sky Kelsey
 */
public class SurveyDialog extends ApptentiveBaseDialog {

	SurveyDefinition surveyDefinition;
	OnActionPerformedListener listener;

	public SurveyDialog(Context context, SurveyDefinition surveyDefinition) {
		super(context, R.layout.apptentive_survey_dialog);
		this.surveyDefinition = surveyDefinition;

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(surveyDefinition.getName());

		String descriptionText = surveyDefinition.getDescription();
		TextView description = (TextView) findViewById(R.id.description);
		if (descriptionText != null) {
			description.setText(descriptionText);
		} else {
			description.setVisibility(View.INVISIBLE);
		}

		final Button send = (Button) findViewById(R.id.send);
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(listener != null) {
					listener.onSurveySubmitted();
				}
			}
		});

		View about = findViewById(R.id.apptentive_branding_view);
		about.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (listener != null) {
					listener.onAboutApptentiveButtonPressed();
				}
			}
		});

		LinearLayout questions = (LinearLayout) findViewById(R.id.questions);

		// Then render all the questions
		for (final Question question : surveyDefinition.getQuestions()) {
			if (question.getType() == Question.QUESTION_TYPE_SINGLELINE) {
				TextSurveyQuestionView2 textQuestionView = new TextSurveyQuestionView2(context, (SinglelineQuestion) question);
				textQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener<TextSurveyQuestionView2>() {
					public void onAnswered(TextSurveyQuestionView2 view) {
						if(listener != null) {
							listener.onQuestionAnswered(question);
						}
						send.setEnabled(isCompleted());
					}
				});
				questions.addView(textQuestionView);
			} else if (question.getType() == Question.QUESTION_TYPE_MULTICHOICE) {
				MultichoiceSurveyQuestionView2 multichoiceQuestionView = new MultichoiceSurveyQuestionView2(context, (MultichoiceQuestion) question);
				multichoiceQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener<MultichoiceSurveyQuestionView2>() {
					public void onAnswered(MultichoiceSurveyQuestionView2 view) {
						if(listener != null) {
							listener.onQuestionAnswered(question);
						}
						send.setEnabled(isCompleted());
					}
				});
				questions.addView(multichoiceQuestionView);
			} else if (question.getType() == Question.QUESTION_TYPE_MULTISELECT) {
				MultiselectSurveyQuestionView2 multiselectQuestionView = new MultiselectSurveyQuestionView2(context, (MultiselectQuestion) question);
				multiselectQuestionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener<MultiselectSurveyQuestionView2>() {
					public void onAnswered(MultiselectSurveyQuestionView2 view) {
						if(listener != null) {
							listener.onQuestionAnswered(question);
						}
						send.setEnabled(isCompleted());
					}
				});
				questions.addView(multiselectQuestionView);
			} else if (question.getType() == Question.QUESTION_TYPE_STACKRANK) {
				StackrankSurveyQuestionView questionView = new StackrankSurveyQuestionView(context, (StackrankQuestion) question);
				questionView.setOnSurveyQuestionAnsweredListener(new OnSurveyQuestionAnsweredListener() {
					public void onAnswered(Object view) {
						if(listener != null) {
							listener.onQuestionAnswered(question);
						}
						// TODO: This.
					}
				});
				questions.addView(questionView);
			}
		}
	}

	boolean isCompleted() {
		return SurveyModule.getInstance().isCompleted();
	}

	public void setOnActionPerformedListener(OnActionPerformedListener listener) {
		this.listener = listener;
	}

	public interface OnActionPerformedListener {
		public void onAboutApptentiveButtonPressed();

		public void onSurveySubmitted();

		public void onSurveySkipped();

		public void onQuestionAnswered(Question question);
	}
}
