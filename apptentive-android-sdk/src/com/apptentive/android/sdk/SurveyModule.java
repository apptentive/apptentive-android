/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.module.survey.AnswerDefinition;
import com.apptentive.android.sdk.module.survey.QuestionDefinition;
import com.apptentive.android.sdk.module.survey.SurveyDefinition;
import com.apptentive.android.sdk.offline.PayloadManager;
import com.apptentive.android.sdk.offline.SurveyPayload;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

/**
 * This module is responsible for fetching, displaying, and sending finished survey payloads to the apptentive server.
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

	private SurveyModule() {
		surveyDefinition = null;
		fetchSurvey();
	}

	/**
	 * Asynchronously download surveys and put them in the model.
	 */
	public void fetchSurvey() {
		// Upload any payloads that were created while the device was offline.
		new Thread() {
			public void run() {
				ApptentiveClient client = new ApptentiveClient(GlobalInfo.apiKey);
				setSurvey(client.getSurvey());
			}
		}.start();
	}

	private void setSurvey(SurveyDefinition surveyDefinition) {
		this.surveyDefinition = surveyDefinition;
	}


	// *************************************************************************************************
	// ******************************************* Not Private *****************************************
	// *************************************************************************************************

	public void show(Context context) {
		if(!hasSurvey()){
			return;
		}
		Intent intent = new Intent();
		intent.setClass(context, ApptentiveActivity.class);
		intent.putExtra("module", ApptentiveActivity.Module.SURVEY.toString());
		context.startActivity(intent);
	}

	public boolean hasSurvey() {
		return (surveyDefinition != null);
	}

	public void cleanup() {
		this.surveyDefinition = null;
		this.result = null;
	}

	void setAnswer(Activity activity, int questionIndex, String answer) {
		result.setAnswer(questionIndex, answer);
		Button skipSend = (Button) activity.findViewById(R.id.apptentive_survey_button_send);
		if (result.hasBeenAnswered()) {
			skipSend.setText(R.string.apptentive_send);
		} else {
			skipSend.setText(R.string.apptentive_skip);
		}
	}

	void doShow(final Activity activity) {
		if (surveyDefinition == null) {
			return;
		}
		result = new SurveyPayload(surveyDefinition);

		TextView name = (TextView) activity.findViewById(R.id.apptentive_survey_title_text);
		name.setText(surveyDefinition.getName());

		View descriptionBox = activity.findViewById(R.id.apptentive_survey_description_box);

		if (surveyDefinition.getDescription() != null) {
			TextView description = (TextView) activity.findViewById(R.id.apptentive_survey_description_text);
			description.setText(surveyDefinition.getDescription());
		} else {
			((ViewGroup) descriptionBox.getParent()).removeView(descriptionBox);
		}

		Button sendButton = (Button) activity.findViewById(R.id.apptentive_survey_button_send);
		sendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Util.hideSoftKeyboard(activity, view);
				PayloadManager.getInstance().putPayload(result);
				cleanup();
				activity.finish();
			}
		});

		View brandingButton = activity.findViewById(R.id.apptentive_branding_view);
		brandingButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				AboutModule.getInstance().show(activity);
			}
		});

		TextView surveyTitle = (TextView) activity.findViewById(R.id.apptentive_survey_title_text);
		surveyTitle.setFocusable(true);
		surveyTitle.setFocusableInTouchMode(true);
		surveyTitle.setText(surveyDefinition.getName());

		LinearLayout questionList = (LinearLayout) activity.findViewById(R.id.aptentive_survey_question_list);

		for (QuestionDefinition question : surveyDefinition.getQuestions()) {
			int index = surveyDefinition.getQuestions().indexOf(question);

			View questionRow = activity.getLayoutInflater().inflate(R.layout.apptentive_question, null);

			TextView questionNumber = (TextView) questionRow.findViewById(R.id.apptentive_question_number);
			TextView questionValue = (TextView) questionRow.findViewById(R.id.apptentive_question_value);
			LinearLayout answerView = (LinearLayout) questionRow.findViewById(R.id.apptentive_question_answer_view);

			questionNumber.setText(index + 1 + "");
			questionValue.setText(question.getValue());

			switch (question.getType()) {
				case singleline:
					EditText editText = new EditText(activity);
					editText.setLayoutParams(Constants.rowLayout);

					class TextAnswerTextWatcher implements TextWatcher {
						private int listItem;

						private TextAnswerTextWatcher(int listItem) {
							this.listItem = listItem;
						}

						public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
						}

						public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
						}

						public void afterTextChanged(Editable editable) {
							setAnswer(activity, listItem, editable.toString());
						}
					}

					editText.addTextChangedListener(new TextAnswerTextWatcher(index));
					answerView.addView(editText);
					break;
				case multichoice:
					List<String> optionNames = new ArrayList<String>();
					int selected = 0;
					List<AnswerDefinition> answerDefinitions = question.getAnswerChoices();
					optionNames.add(QuestionDefinition.DEFAULT);
					for (int i = 0; i < answerDefinitions.size(); i++) {
						optionNames.add(answerDefinitions.get(i).getValue());
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, optionNames);
					adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					Spinner spinner = new Spinner(activity);
					spinner.setLayoutParams(Constants.rowLayout);
					spinner.setPrompt("Choose one...");
					spinner.setAdapter(adapter);

					class DropdownListener implements AdapterView.OnItemSelectedListener {
						int listItem;

						public DropdownListener(int listItem) {
							this.listItem = listItem;
						}

						public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
							TextView selected = (TextView) view;
							String answer = selected.getText().toString();
							setAnswer(activity, listItem, answer);
						}

						public void onNothingSelected(AdapterView<?> adapterView) {
						}
					}

					spinner.setOnItemSelectedListener(new DropdownListener(index));
					spinner.setSelection(selected);
					answerView.addView(spinner);
					break;
				default:
					break;
			}
			questionList.addView(questionRow);
		}
		// Force the top of the survey to be shown first.
		surveyTitle.requestFocus();
	}
}
