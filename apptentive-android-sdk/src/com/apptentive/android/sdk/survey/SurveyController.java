/*
 * SurveyController.java
 *
 * Created by Sky Kelsey on 2011-10-08.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.survey;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.apptentive.android.sdk.ALog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.ApptentiveModel;
import com.apptentive.android.sdk.model.ViewController;
import com.apptentive.android.sdk.offline.JSONPayload;
import com.apptentive.android.sdk.offline.PayloadManager;
import com.apptentive.android.sdk.offline.Survey;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import java.util.ArrayList;
import java.util.List;

public class SurveyController implements ViewController {

	private final ALog log = new ALog(this.getClass());
	private Activity activity;
	private SurveyDefinition definition;
	private Survey result;
	private boolean answered = false;

	public SurveyController(Activity activity) {
		this.activity = activity;
		ApptentiveModel model = ApptentiveModel.getInstance();
		definition = model.getSurvey();
		this.result = new Survey(definition);
		setupForm();
	}

	private void setupForm() {
		TextView name = (TextView) activity.findViewById(R.id.apptentive_survey_title_text);
		name.setText(definition.getName());

		View descriptionBox = activity.findViewById(R.id.apptentive_survey_description_box);

		if(definition.getDescription() != null){
			TextView description = (TextView) activity.findViewById(R.id.apptentive_survey_description_text);
			description.setText(definition.getDescription());
		}else{
			((ViewGroup)descriptionBox.getParent()).removeView(descriptionBox);
		}

		Button send = (Button) activity.findViewById(R.id.apptentive_survey_button_send);
		send.setOnClickListener(clickListener);

		View branding = activity.findViewById(R.id.apptentive_branding_view);
		branding.setOnClickListener(clickListener);

		Button okay = (Button) activity.findViewById(R.id.apptentive_button_about_okay);
		okay.setOnClickListener(clickListener);

		TextView surveyTitle = (TextView) activity.findViewById(R.id.apptentive_survey_title_text);
		surveyTitle.setFocusable(true);
		surveyTitle.setFocusableInTouchMode(true);
		surveyTitle.setText(definition.getName());

		LinearLayout questionList = (LinearLayout) activity.findViewById(R.id.aptentive_survey_question_list);

		for (QuestionDefinition question : definition.getQuestions()) {
			int index = definition.getQuestions().indexOf(question);

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
					ArrayAdapter adapter = new ArrayAdapter(activity, android.R.layout.simple_spinner_item, optionNames);
					adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					Spinner spinner = new Spinner(activity);
					spinner.setLayoutParams(Constants.rowLayout);
					spinner.setPrompt("Choose one...");
					spinner.setAdapter(adapter);
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

	public void cleanup() {

	}

	private void send(JSONPayload payload) {
		PayloadManager payloadManager = new PayloadManager(activity.getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE));
		payloadManager.save(payload);
		payloadManager.run();
	}

	void setAnswer(int questionIndex, String answer) {
		result.setAnswer(questionIndex, answer);
		setAnswered(result.hasBeenAnswered());
	}

	/**
	 * This is used to change what the send button says and does.
	 *
	 * @param answered
	 */
	void setAnswered(boolean answered) {
		Button skipSend = (Button) activity.findViewById(R.id.apptentive_survey_button_send);
		this.answered = answered;
		if (this.answered) {
			skipSend.setText(R.string.apptentive_send);
		} else {
			skipSend.setText(R.string.apptentive_skip);
		}
	}

	// Listener classes

	private View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			ViewFlipper aboutFlipper = (ViewFlipper) activity.findViewById(R.id.apptentive_activity_about_flipper);

			Util.hideSoftKeyboard(activity, view);

			switch (view.getId()) {
				case R.id.apptentive_survey_button_send:
					if (answered) {
						send(result);
					}
					activity.finish();
					break;
				case R.id.apptentive_branding_view:
					aboutFlipper.setInAnimation(Constants.inFromBottomAnimation());
					aboutFlipper.setOutAnimation(Constants.outToTopAnimation());
					aboutFlipper.showNext();
					break;
				case R.id.apptentive_button_about_okay:
					aboutFlipper.setInAnimation(Constants.inFromTopAnimation());
					aboutFlipper.setOutAnimation(Constants.outToBottomAnimation());
					aboutFlipper.showPrevious();
					break;
				default:
					break;
			}
		}
	};

	private class DropdownListener implements AdapterView.OnItemSelectedListener {
		int listItem;

		public DropdownListener(int listItem) {
			this.listItem = listItem;
		}

		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
			TextView selected = (TextView) view;
			String answer = selected.getText().toString();
			setAnswer(listItem, answer);
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	}

	private class TextAnswerTextWatcher implements TextWatcher {
		private int listItem;

		private TextAnswerTextWatcher(int listItem) {
			this.listItem = listItem;
		}

		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		}

		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		}

		public void afterTextChanged(Editable editable) {
			String answer = editable.toString();
			setAnswer(listItem, answer);
		}
	}
}