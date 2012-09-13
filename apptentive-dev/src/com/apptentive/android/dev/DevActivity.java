/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.dev;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.apptentive.android.sdk.*;
import com.apptentive.android.sdk.module.survey.OnSurveyFetchedListener;

/**
 * @author Sky Kelsey
 */
public class DevActivity extends ApptentiveActivity {

	private static final String LOG_TAG = "Apptentive Testing App";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// BEGIN APPTENTIVE INITIALIZATION
		// OPTIONAL: To specify a different user email than what the device was setup with.
		//Apptentive.setUserEmail("user_email@example.com");
		// OPTIONAL: To send extra data with your feedback.
		Apptentive.getFeedbackModule().addDataField("username", "Sky Kelsey");
		// END APPTENTIVE INITIALIZATION

		// Setup UI:
		final RatingModule ratingModule = Apptentive.getRatingModule();
		final FeedbackModule feedbackModule = Apptentive.getFeedbackModule();

		Button testsButton = (Button) findViewById(R.id.button_tests);
		testsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent testsIntent = new Intent();
				testsIntent.setClass(DevActivity.this, TestsActivity.class);
				startActivity(testsIntent);
			}
		});
		Button resetButton = (Button) findViewById(R.id.button_reset);
		resetButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				ratingModule.reset();
			}
		});
		Button eventButton = (Button) findViewById(R.id.button_event);
		eventButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				ratingModule.logEvent();
			}
		});
		Button dayButton = (Button) findViewById(R.id.button_day);
		dayButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				ratingModule.day();
			}
		});
		Button choiceButton = (Button) findViewById(R.id.button_choice);
		choiceButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				ratingModule.forceShowEnjoymentDialog(DevActivity.this);
			}
		});
		Button ratingsButton = (Button) findViewById(R.id.button_ratings);
		ratingsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				ratingModule.showRatingDialog(DevActivity.this);
			}
		});
		Button feedbackButton = (Button) findViewById(R.id.button_feedback);
		feedbackButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				feedbackModule.forceShowFeedbackDialog(DevActivity.this);
			}
		});

		Button fetchSurveyButton = (Button) findViewById(R.id.button_survey_fetch);
		final Button showSurveyButton = (Button) findViewById(R.id.button_survey_show);
		fetchSurveyButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				final SurveyModule surveyModule = Apptentive.getSurveyModule();
				surveyModule.fetchSurvey(new OnSurveyFetchedListener() {
					public void onSurveyFetched(final boolean success) {
						Log.e(LOG_TAG, "onSurveyFetched(" + success + ")");
						runOnUiThread(new Runnable() {
							public void run() {
								Toast toast = Toast.makeText(DevActivity.this, success ? "Survey fetch successful." : "Survey fetch failed.", Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
								showSurveyButton.setEnabled(success);
							}
						});
					}
				});
			}
		});

		showSurveyButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				final SurveyModule surveyModule = Apptentive.getSurveyModule();
				if (surveyModule.isSurveyReady()) {
					surveyModule.show(DevActivity.this);
					showSurveyButton.setEnabled(false);
				}
			}
		});
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			Apptentive.getRatingModule().run(this);
		}
	}
}
