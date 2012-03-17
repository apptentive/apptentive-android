/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.apptentive.android.sdk.*;
import com.apptentive.android.sdk.module.rating.impl.AndroidMarketRatingProvider;
import com.apptentive.android.sdk.module.survey.OnSurveyFetchedListener;

/**
 * @author Sky Kelsey
 */
public class DemoActivity extends Activity {

	private static final String LOG_TAG = "Apptentive";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// BEGIN APPTENTIVE INITIALIZATION

		final Apptentive apptentive = Apptentive.getInstance();
		apptentive.initialize(getApplication(), "<YOUR_API_KEY>");
		apptentive.setAppDisplayName("Apptentive Demo");
		//apptentive.setUserEmail("user_email@example.com");

		final RatingModule ratingModule = apptentive.getRatingModule();
		ratingModule.setRatingProvider(new AndroidMarketRatingProvider());
		// Bump uses each time the app starts.
		ratingModule.logUse();

		// Add custom data fields to feedback this way:
		final FeedbackModule feedbackModule = apptentive.getFeedbackModule();
		feedbackModule.addDataField("foo", "bar");

		// END APPTENTIVE INITIALIZATION


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
				ratingModule.forceShowEnjoymentDialog(DemoActivity.this);
			}
		});
		Button ratingsButton = (Button) findViewById(R.id.button_ratings);
		ratingsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				ratingModule.showRatingDialog(DemoActivity.this);
			}
		});
		Button feedbackButton = (Button) findViewById(R.id.button_feedback);
		feedbackButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				feedbackModule.forceShowFeedbackDialog(DemoActivity.this);
			}
		});

		Button fetchSurveyButton = (Button) findViewById(R.id.button_survey_fetch);
		final Button showSurveyButton = (Button) findViewById(R.id.button_survey_show);
		fetchSurveyButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				final SurveyModule surveyModule = apptentive.getSurveyModule();
				surveyModule.fetchSurvey(new OnSurveyFetchedListener() {
					public void onSurveyFetched(final boolean success) {
						Log.e(LOG_TAG, "onSurveyFetched("+success+")");
						runOnUiThread(new Runnable() {
							public void run() {
								Toast toast = Toast.makeText(DemoActivity.this, success ? "Survey fetch successful." : "Survey fetch failed.", 1000);
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
				final SurveyModule surveyModule = apptentive.getSurveyModule();
				if(surveyModule.isSurveyReady()) {
					surveyModule.show(DemoActivity.this);
					showSurveyButton.setEnabled(false);
				}
			}
		});
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		Log.e(LOG_TAG, "onWindowFocusChanges(" + hasFocus + ")");
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			Apptentive.getInstance().getRatingModule().run(DemoActivity.this);
		}
	}

	@Override
	protected void onDestroy() {
		Apptentive.getInstance().onDestroy();
		super.onDestroy();
	}
}
