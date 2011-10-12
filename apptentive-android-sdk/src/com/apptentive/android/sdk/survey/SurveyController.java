/*
 * SurveyController.java
 *
 * Created by Sky Kelsey on 2011-10-08.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.survey;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import com.apptentive.android.sdk.ALog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.ApptentiveModel;
import com.apptentive.android.sdk.offline.JSONPayload;
import com.apptentive.android.sdk.offline.PayloadManager;
import com.apptentive.android.sdk.offline.Survey;
import com.apptentive.android.sdk.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class SurveyController {

	private final ALog log = new ALog(this.getClass());
	private Activity activity;
	private SurveyDefinition definition;
	private Survey result;

	public SurveyController(Activity activity) {
		this.activity = activity;
		ApptentiveModel model = ApptentiveModel.getInstance();
		List<SurveyDefinition> surveys = model.getSurveys();
		if(surveys != null && surveys.size() > 0){
			log.e("Found survey");
			definition = surveys.get(0);
		}
		this.result = new Survey(definition);
		setupForm();
		log.e("Done");
	}

	private void setupForm() {
		TextView name = (TextView) activity.findViewById(R.id.apptentive_survey_title_text);
		name.setText(definition.getName());
		TextView description = (TextView) activity.findViewById(R.id.apptentive_survey_description_text);
		description.setText(definition.getDescription());

		Button send = (Button) activity.findViewById(R.id.apptentive_survey_button_send);
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				send(result);
				activity.finish();
			}
		});

		//TextView surveyTitle = (TextView) activity.findViewById(R.id.apptentive_survey_title_text);
		//surveyTitle.setText(survey.getName());
		LinearLayout questionList = (LinearLayout) activity.findViewById(R.id.aptentive_survey_question_list);

		for(QuestionDefinition question : definition.getQuestions()){
			int index = definition.getQuestions().indexOf(question);

			View questionRow = activity.getLayoutInflater().inflate(R.layout.apptentive_question, null);

			TextView questionNumber = (TextView) questionRow.findViewById(R.id.apptentive_question_number);
			TextView questionValue = (TextView) questionRow.findViewById(R.id.apptentive_question_value);
			LinearLayout answerView = (LinearLayout) questionRow.findViewById(R.id.apptentive_question_answer_view);

			questionNumber.setText(index+1+"");
			questionValue.setText(question.getValue());

			switch(question.getType()){
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
					for(int i = 0; i < answerDefinitions.size(); i++){
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
	}

		private void send(JSONPayload payload) {
		PayloadManager payloadManager = new PayloadManager(activity);
		payloadManager.save(payload);
		payloadManager.run();
	}
	void setAnswer(int questionIndex, String answer){
		log.e("For question: " + questionIndex +", setting answer to: " + answer);
		result.setAnswer(questionIndex, answer);
	}

	private class DropdownListener implements AdapterView.OnItemSelectedListener {
		int listItem;

		public DropdownListener(int listItem){
			this.listItem = listItem;
		}

		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
			log.e("Item selected: " + view.toString() + ", for item: " + listItem);
			TextView selected = (TextView) view;
			String answer = selected.getText().toString();
			setAnswer(listItem, answer);
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
			log.e("Nothing selected.");
		}
	}

	private class TextAnswerTextWatcher implements TextWatcher {
		private int listItem;

		private TextAnswerTextWatcher(int listItem) {
			this.listItem = listItem;
		}

		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

		public void afterTextChanged(Editable editable) {
			String answer = editable.toString();
			setAnswer(listItem, answer);
		}
	}
}