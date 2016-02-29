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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.AnswerDefinition;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.MultiselectQuestion;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class MultiselectSurveyQuestionView extends BaseSurveyQuestionView<MultiselectQuestion> implements CompoundButton.OnCheckedChangeListener {

	LinearLayout choiceContainer;
	private ArrayList<Integer> selectedItems;

	public static MultiselectSurveyQuestionView newInstance(MultiselectQuestion question) {

		MultiselectSurveyQuestionView f = new MultiselectSurveyQuestionView();

		Bundle b = new Bundle();
		b.putString("question", question.toString());
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();

		if (bundle != null) {
			try {
				question = new MultiselectQuestion(bundle.getString("question"));
			} catch (JSONException e) {

			}
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
													 Bundle savedInstanceState)  {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		Context contextThemeWrapper = new ContextThemeWrapper(getContext(), ApptentiveInternal.apptentiveTheme);
		LayoutInflater themedInflater = LayoutInflater.from(contextThemeWrapper);

		View questionView = themedInflater.inflate(R.layout.apptentive_survey_question_multiselect, getAnswerContainer(v));

		List<AnswerDefinition> answerDefinitions = question.getAnswerChoices();
		choiceContainer = (LinearLayout) questionView.findViewById(R.id.choice_container);
		if (selectedItems == null) {
			selectedItems = new ArrayList<>();
		}

		for (int i = 0; i < answerDefinitions.size(); i++) {
			CheckBox choice = (CheckBox) inflater.inflate(R.layout.apptentive_survey_question_multiselect_choice, choiceContainer, false);
			AnswerDefinition answerDefinition = answerDefinitions.get(i);
			choice.setText(answerDefinition.getValue());
			choice.setTag(R.id.apptentive_survey_answer_id, answerDefinition.getId());
			choice.setTag(R.id.apptentive_survey_answer_index, i);
			if (selectedItems.contains(i)) {
				choice.setChecked(true);
			}
			choice.setOnCheckedChangeListener(this);
			choiceContainer.addView(choice);
		}

		return v;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int index = (int)buttonView.getTag(R.id.apptentive_survey_answer_index);
		boolean bFound = selectedItems.contains(index);
		if (isChecked && !bFound) {
			selectedItems.add(Integer.valueOf(index));
		} else if (!isChecked && bFound){
			selectedItems.remove(Integer.valueOf(index));
		}

		if (getContext() instanceof Activity) {
			Util.hideSoftKeyboard(getContext(), MultiselectSurveyQuestionView.this.getView());
		}
		fireListener();
	}

	@Override
	public boolean isValid() {
		int checkedBoxes = 0;
		for (int i = 0; i < choiceContainer.getChildCount(); i++) {
			CheckBox checkBox = (CheckBox) choiceContainer.getChildAt(i);
			if (checkBox.isChecked()) {
				checkedBoxes++;
			}
		}
		// If it is answered at all, it must be answered properly.
		return
				(!question.isRequired() && checkedBoxes == 0) ||
						((question.getMinSelections() <= checkedBoxes) && (checkedBoxes <= question.getMaxSelections()));
	}

	@Override
	public Object getAnswer() {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < choiceContainer.getChildCount(); i++) {
			CheckBox checkBox = (CheckBox) choiceContainer.getChildAt(i);
			if (checkBox.isChecked()) {
				jsonArray.put(checkBox.getTag(R.id.apptentive_survey_answer_id));
			}
		}
		return jsonArray;
	}
}
