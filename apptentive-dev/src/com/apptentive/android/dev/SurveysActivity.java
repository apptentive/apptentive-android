/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.dev;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveActivity;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;

/**
 * @author Sky Kelsey
 */
public class SurveysActivity extends ApptentiveActivity {

	private String selectedTag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.surveys);


		// Set up a spinner to choose which tag we will use to show a survey.
		Spinner surveySpinner = (Spinner) findViewById(R.id.survey_spinner);
		surveySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				if (i == 0) {
					selectedTag = null;
				} else {
					String[] tagsArray = getResources().getStringArray(R.array.survey_tags);
					selectedTag = tagsArray[i];
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				selectedTag = null;
			}
		});
	}

	public void showSurvey(@SuppressWarnings("unused") View view) {
		OnSurveyFinishedListener listener = new OnSurveyFinishedListener() {
			public void onSurveyFinished(boolean completed) {
				Log.e(MainActivity.LOG_TAG, "A survey finished, and was " + (completed ? "completed" : "skipped"));
			}
		};

		boolean ret;
		if (selectedTag != null) {
			ret = Apptentive.showSurvey(this, listener, selectedTag);
		} else {
			ret = Apptentive.showSurvey(this, listener);
		}
		if (!ret) {
			Toast.makeText(this, "No matching survey found.", Toast.LENGTH_SHORT).show();
		}
	}
}
