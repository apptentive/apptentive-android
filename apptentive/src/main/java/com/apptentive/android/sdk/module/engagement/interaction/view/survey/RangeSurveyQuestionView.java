/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.RangeQuestion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;


public class RangeSurveyQuestionView extends BaseSurveyQuestionView<RangeQuestion> implements RadioButton.OnCheckedChangeListener {

	private RadioGroup radioGroup;
	private int min;
	private int max;
	private String minLabel;
	private String maxLabel;

	// TODO: Save these
	private boolean valueWasSelected;
	private int selectedValue;

	public static RangeSurveyQuestionView newInstance(RangeQuestion question) {
		RangeSurveyQuestionView f = new RangeSurveyQuestionView();
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
				question = new RangeQuestion(bundle.getString("question"));
			} catch (JSONException e) {
			}
		}
		min = question.getMin();
		max = question.getMax();
		minLabel = question.getMinLabel();
		maxLabel = question.getMaxLabel();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		ViewGroup answerContainer = getAnswerContainer(v);
		ViewGroup answer = (ViewGroup) inflater.inflate(R.layout.apptentive_survey_question_range_answer, answerContainer, false);
		answerContainer.addView(answer);

		if (!TextUtils.isEmpty(minLabel)) {
			TextView minLabelTextView = (TextView) answer.findViewById(R.id.min_label);
			minLabelTextView.setText(minLabel);
		}
		if (!TextUtils.isEmpty(maxLabel)) {
			TextView maxLabelTextView = (TextView) answer.findViewById(R.id.max_label);
			maxLabelTextView.setText(maxLabel);
		}

		radioGroup = (RadioGroup) answer.findViewById(R.id.range_container);

		NumberFormat defaultNumberFormat = NumberFormat.getInstance();

		for (int i = min; i <= max; i++) {
			try {
				RadioButton radioButton = (RadioButton) inflater.inflate(R.layout.apptentive_survey_question_range_choice, radioGroup, false);
				radioButton.setText(defaultNumberFormat.format(i));
				radioButton.setTag(i);
				radioGroup.addView(radioButton);
			} catch (Throwable e) {
				String message = "Error";
				while (e != null) {
					ApptentiveLog.e(message, e);
					message = " caused by:";
					e = e.getCause();
				}
				throw new RuntimeException(e);
			}
		}
		return v;
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();

		for (int i = 0; i < radioGroup.getChildCount(); i++) {
			RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
			radioButton.setOnCheckedChangeListener(this);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean isValid() {
		// TODO
		boolean valid = !question.isRequired() || false;
		return valid;
	}

	@Override
	public Object getAnswer() {
		if (valueWasSelected) {
			try {
				JSONArray jsonArray = new JSONArray();
				JSONObject jsonObject = new JSONObject();
				jsonArray.put(jsonObject);
				jsonObject.put("value", selectedValue);
				return jsonArray;
			} catch (JSONException e) {
				// Return null;
			}
		}
		return null;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		selectedValue = (int) buttonView.getTag();
		valueWasSelected = true;
	}
}
