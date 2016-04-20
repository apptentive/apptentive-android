/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.AnswerDefinition;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.MultichoiceQuestion;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;

import java.util.*;


public class MultichoiceSurveyQuestionView extends BaseSurveyQuestionView<MultichoiceQuestion> implements RadioGroup.OnCheckedChangeListener {

	RadioGroup radioGroup;
	boolean buttonChecked;
	private int selectItem;
	private final static String SELECTED_RADIO_BUTTON_STATE = "selectedRadioButton";

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
		selectItem = -1;
		Bundle bundle = getArguments();

		if (bundle != null) {
			try {
				question = new MultichoiceQuestion(bundle.getString("question"));
			} catch (JSONException e) {

			}
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		Context contextThemeWrapper = new ContextThemeWrapper(getContext(), ApptentiveInternal.getInstance().getApptentiveTheme());
		LayoutInflater themedInflater = LayoutInflater.from(contextThemeWrapper);

		List<AnswerDefinition> answerDefinitions = question.getAnswerChoices();

		View questionView = themedInflater.inflate(R.layout.apptentive_survey_question_multichoice, getAnswerContainer(v));

		radioGroup = (RadioGroup) questionView.findViewById(R.id.radio_group);

		for (int i = 0; i < answerDefinitions.size(); i++) {
			RadioButton choice = (RadioButton) themedInflater.inflate(R.layout.apptentive_survey_question_multichoice_choice, radioGroup, false);
			AnswerDefinition answerDefinition = answerDefinitions.get(i);
			choice.setText(answerDefinition.getValue());
			choice.setTag(R.id.apptentive_survey_answer_id, answerDefinition.getId());
			radioGroup.addView(choice);
		}
		radioGroup.setOnCheckedChangeListener(null);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		radioGroup.setOnCheckedChangeListener(this);
		selectItem = (savedInstanceState == null) ? -1 : savedInstanceState.getInt(SELECTED_RADIO_BUTTON_STATE, -1);
		for (int i = 0; i < radioGroup.getChildCount(); i++) {
			RadioButton choice = (RadioButton) radioGroup.getChildAt(i);
			if (i == selectItem) {
				choice.setChecked(true);
			}
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		buttonChecked = true;
		RadioButton checkedRadioButton = (RadioButton)group.findViewById(checkedId);
		selectItem = group.indexOfChild(checkedRadioButton);
		if (getContext() instanceof Activity) {
			Util.hideSoftKeyboard(getContext(), MultichoiceSurveyQuestionView.this.getView());
		}
		fireListener();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(SELECTED_RADIO_BUTTON_STATE, selectItem);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean isValid() {
		return !question.isRequired() || buttonChecked;
	}

	@Override
	public Object getAnswer() {
		for (int i = 0; i < radioGroup.getChildCount(); i++) {
			RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
			if (radioButton.isChecked()) {
				return radioButton.getTag(R.id.apptentive_survey_answer_id);
			}
		}
		return null;
	}
}
