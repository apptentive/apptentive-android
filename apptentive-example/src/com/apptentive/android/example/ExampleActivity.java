/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.example;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveActivity;
import com.apptentive.android.sdk.module.survey.OnSurveyCompletedListener;
import com.apptentive.android.sdk.module.survey.OnSurveyFetchedListener;

/**
 * This is an example Application, demonstrating the most straight-forward Apptentive integration path: inheriting
 * from {@link ApptentiveActivity}.
 *
 * @author Sky Kelsey
 */
public class ExampleActivity extends ApptentiveActivity {

	private static String LOG_TAG = "Apptentive Example";

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			Apptentive.getRatingModule().run(this);
		}
	}

	public void onFeedbackButtonPressed(View view) {
		Apptentive.getFeedbackModule().forceShowFeedbackDialog(this);
	}

	public void onFetchSurveyButtonPressed(View view) {
		Apptentive.getSurveyModule().fetchSurvey(new OnSurveyFetchedListener() {
			public void onSurveyFetched(final boolean success) {
				Log.e(LOG_TAG, "onSurveyFetched(" + success + ")");
				runOnUiThread(new Runnable() {
					public void run() {
						Toast toast = Toast.makeText(ExampleActivity.this, success ? "Survey fetch successful." : "Survey fetch failed.", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
						findViewById(R.id.show_survey_button).setEnabled(success);
					}
				});
			}
		});
	}

	public void onShowSurveyButtonPressed(View view) {
		Apptentive.getSurveyModule().show(this, new OnSurveyCompletedListener() {
			public void onSurveyCompletedListener() {
				Log.e(LOG_TAG, "Got a callback from completed survey!");
			}
		});
	}
}
