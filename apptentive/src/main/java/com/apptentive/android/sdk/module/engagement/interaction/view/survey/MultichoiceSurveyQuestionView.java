/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.AnswerDefinition;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.MultichoiceQuestion;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;


public class MultichoiceSurveyQuestionView extends BaseSurveyQuestionView<MultichoiceQuestion> implements SurveyQuestionChoice.OnCheckedChangeListener, SurveyQuestionChoice.OnOtherTextChangedListener {

	LinearLayout choiceContainer;
	protected HashSet<Integer> selectedChoices;
	// Used to store the text entered in the "other" field. Will be empty for choices of a different type.
	protected HashMap<Integer, String> otherState;

	private final static String SELECTED_CHOICES = "selectedChoices";
	private final static String OTHER_STATE = "otherState";

	public static MultichoiceSurveyQuestionView newInstance(MultichoiceQuestion question) {
		MultichoiceSurveyQuestionView f = new MultichoiceSurveyQuestionView();
		Bundle b = new Bundle();
		b.putString("question", question.toString());
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selectedChoices = new HashSet<>();
		otherState = new HashMap<>();
		Bundle bundle = getArguments();
		if (bundle != null) {
			try {
				question = new MultichoiceQuestion(bundle.getString("question"));
			} catch (JSONException e) {
				logException(e);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		try {
			List<AnswerDefinition> answerDefinitions = question.getAnswerChoices();

			View questionView = inflater.inflate(R.layout.apptentive_survey_question_multichoice, getAnswerContainer(v));

			choiceContainer = (LinearLayout) questionView.findViewById(R.id.choice_container);

			if (savedInstanceState != null) {
				selectedChoices = (HashSet<Integer>) savedInstanceState.getSerializable(SELECTED_CHOICES);
				otherState = (HashMap<Integer, String>) savedInstanceState.getSerializable(OTHER_STATE);
			}

			for (int i = 0; i < answerDefinitions.size(); i++) {
				AnswerDefinition answerDefinition = answerDefinitions.get(i);
				SurveyQuestionChoice choice = new SurveyQuestionChoice(getContext(), inflater, answerDefinition, question.getType(), i);
				if (selectedChoices.contains(i)) {
					choice.setChecked(true);
				}
				if (answerDefinition.getType().equals(AnswerDefinition.TYPE_OTHER)) {
					choice.setOtherText(otherState.get(i));
				}
				choice.setOnCheckChangedListener(this);
				choice.setOnOtherTextChangedListener(this);
				choiceContainer.addView(choice);
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception in %s.onCreateView()", MultichoiceSurveyQuestionView.class);
			logException(e);
		}
		return v;
	}

	@Override
	public void onCheckChanged(SurveyQuestionChoice choice, boolean isChecked) {
		// Update saved state
		selectedChoices.clear();
		if (isChecked) {
			// Clear the other choices
			for (int i = 0; i < choiceContainer.getChildCount(); i++) {
				SurveyQuestionChoice currentChoice = (SurveyQuestionChoice) choiceContainer.getChildAt(i);
				if (currentChoice != choice) {
					currentChoice.setChecked(false);
				}
			}
			// Then set this one as the selected choice
			selectedChoices.add(choice.getIndex());
		}
		if (getContext() instanceof Activity) {
			Util.hideSoftKeyboard(getContext(), MultichoiceSurveyQuestionView.this.getView());
		}
		fireListener();
	}

	@Override
	public void onOtherTextChanged(SurveyQuestionChoice choice, String text) {
		int index = choice.getIndex();
		if (index != -1) {
			otherState.put(index, text);
		}
		fireListener();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(SELECTED_CHOICES, selectedChoices);
		outState.putSerializable(OTHER_STATE, otherState);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean isValid() {
		// Make sure all questions are valid.
		for (int i = 0; i < choiceContainer.getChildCount(); i++) {
			SurveyQuestionChoice surveyQuestionChoice = (SurveyQuestionChoice) choiceContainer.getChildAt(i);
			if (!surveyQuestionChoice.isValid(question.isRequired())) {
				return false;
			}
		}
		// Then make sure the number of answers is valid.
		return !question.isRequired() || selectedChoices.size() == 1;
	}

	@Override
	public Object getAnswer() {
		try {
			for (int i = 0; i < choiceContainer.getChildCount(); i++) {
				SurveyQuestionChoice surveyQuestionChoice = (SurveyQuestionChoice) choiceContainer.getChildAt(i);
				if (surveyQuestionChoice.isChecked()) {
					JSONArray jsonArray = new JSONArray();
					JSONObject jsonObject = (JSONObject) surveyQuestionChoice.getAnswer();
					jsonArray.put(jsonObject);
					return jsonArray;
				}
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Error getting survey answer.");
			logException(e);
		}
		return null;
	}
}
