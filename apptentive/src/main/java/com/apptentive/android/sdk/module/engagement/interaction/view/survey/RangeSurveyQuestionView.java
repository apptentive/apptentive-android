/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.os.Bundle;
import androidx.annotation.Nullable;
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
import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;


public class RangeSurveyQuestionView extends BaseSurveyQuestionView<RangeQuestion> implements RadioButton.OnCheckedChangeListener {

	private static final NumberFormat defaultNumberFormat = NumberFormat.getInstance();
	private static final String KEY_VALUE_WAS_SELECTED = "value_was_selected";
	private static final String KEY_SELECTED_VALUE = "selected_value";

	private int min;
	private int max;
	private String minLabel;
	private String maxLabel;

	private RadioGroup radioGroup;

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
				logException(e);
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
		try {
			ViewGroup answerContainer = getAnswerContainer(v);
			ViewGroup answer = (ViewGroup) inflater.inflate(R.layout.apptentive_survey_question_range_answer, answerContainer, false);
			answerContainer.addView(answer);

			TextView minLabelTextView = (TextView) answer.findViewById(R.id.min_label);
			if (!TextUtils.isEmpty(minLabel)) {
				minLabelTextView.setText(minLabel);
			} else {
				minLabelTextView.setVisibility(View.GONE);
			}
			TextView maxLabelTextView = (TextView) answer.findViewById(R.id.max_label);
			if (!TextUtils.isEmpty(maxLabel)) {
				maxLabelTextView.setText(maxLabel);
			} else {
				maxLabelTextView.setVisibility(View.GONE);
			}

			radioGroup = (RadioGroup) answer.findViewById(R.id.range_container);

			for (int i = min; i <= max; i++) {
				RadioButton radioButton = (RadioButton) inflater.inflate(R.layout.apptentive_survey_question_range_choice, radioGroup, false);
				radioButton.setText(defaultNumberFormat.format(i));
				radioButton.setTag(i);
				radioButton.setOnCheckedChangeListener(this);
				radioButton.setContentDescription(StringUtils.format("%s where %s is %s and %s is %s", defaultNumberFormat.format(i), min, minLabel, max, maxLabel));
				radioGroup.addView(radioButton);
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception in %s.onCreateView()", RangeSurveyQuestionView.class.getSimpleName());
			logException(e);
		}
		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(KEY_VALUE_WAS_SELECTED, valueWasSelected);
		outState.putInt(KEY_SELECTED_VALUE, selectedValue);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);

		// Restore instance state after the fragment tries to to avoid any UI weirdness.
		if (savedInstanceState != null) {
			valueWasSelected = savedInstanceState.getBoolean(KEY_VALUE_WAS_SELECTED, false);
			selectedValue = savedInstanceState.getInt(KEY_SELECTED_VALUE, 0);
		}

		for (int i = 0; i < radioGroup.getChildCount(); i++) {
			RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
			if (valueWasSelected && (int) radioButton.getTag() == selectedValue) {
				radioButton.setChecked(true);
				return;
			}
		}
	}

	@Override
	public boolean isValid() {
		return !question.isRequired() || valueWasSelected;
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
				logException(e);
			}
		}
		return null;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			selectedValue = (int) buttonView.getTag();
			valueWasSelected = true;
			fireListener();
		}
	}
}
