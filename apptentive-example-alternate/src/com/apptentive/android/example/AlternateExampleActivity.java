/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;

/**
 * This is an example of how to integrate Apptentive into your Application if you are not able to inherit from
 * {@link com.apptentive.android.sdk.ApptentiveActivity}. In this case, you must hook Apptentive up to your Activity's
 * onStart(), and onStop() methods.
 *
 * @author Sky Kelsey
 */
public class AlternateExampleActivity extends Activity {

	private static String LOG_TAG = "Alternate Apptentive Example";

	private String selectedTag;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

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
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		Apptentive.onStart(this);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			Apptentive.showRatingFlowIfConditionsAreMet(this);
		}

	}

	@Override
	protected void onStop() {
		Apptentive.onStop(this);
		super.onStop();
	}

	public void onMessageCenterButtonPressed(@SuppressWarnings("unused") View view) {
		Apptentive.showMessageCenter(this);
	}

	public void onShowSurveyButtonPressed(@SuppressWarnings("unused") View view) {
		OnSurveyFinishedListener listener = new OnSurveyFinishedListener() {
			public void onSurveyFinished(boolean completed) {
				Log.e(LOG_TAG, "A survey finished, and was " + (completed ? "completed" : "skipped"));
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
