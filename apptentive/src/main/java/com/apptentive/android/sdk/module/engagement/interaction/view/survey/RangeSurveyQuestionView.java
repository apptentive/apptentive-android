/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.RangeQuestion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class RangeSurveyQuestionView extends BaseSurveyQuestionView<RangeQuestion> {

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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		inflater.inflate(R.layout.apptentive_survey_question_range, getAnswerContainer(v));
		return v;
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
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
		int value = 10; // TODO: Get real value
		try {
			JSONArray jsonArray = new JSONArray();
			JSONObject jsonObject = new JSONObject();
			jsonArray.put(jsonObject);
			jsonObject.put("value", value);
			return jsonArray;
		} catch (JSONException e) {
			// Return null;
		}
		return null;
	}
}
