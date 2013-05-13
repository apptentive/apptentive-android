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
import com.apptentive.android.sdk.module.messagecenter.UnreadMessagesListener;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;
import com.apptentive.android.sdk.module.survey.OnSurveyFetchedListener;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class DevActivity extends ApptentiveActivity {

	private static final String LOG_TAG = "Apptentive Dev App";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// *** BEGIN APPTENTIVE INITIALIZATION

		// OPTIONAL: To specify a different user email than what the device was setup with.
		//Apptentive.setUserEmail("user_email@example.com");

		// OPTIONAL: To send extra about the app to the server.
		Map<String, String> customData = new HashMap<String, String>();
		customData.put("user-id", "1234567890");
		customData.put("user-email", "sky@apptentive.com");
		Apptentive.setCustomData(customData);

		// OPTIONAL: Specify a different rating provider if your app is not served from Google Play.
		//Apptentive.setRatingProvider(new AmazonAppstoreRatingProvider());

		// *** END APPTENTIVE INITIALIZATION


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
				DevDebugHelper.resetRatingFlow();
			}
		});
		Button eventButton = (Button) findViewById(R.id.button_event);
		eventButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Apptentive.logSignificantEvent();
			}
		});
		Button dayButton = (Button) findViewById(R.id.button_day);
		dayButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				DevDebugHelper.resetRatingFlow();
			}
		});
		Button choiceButton = (Button) findViewById(R.id.button_choice);
		choiceButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				DevDebugHelper.forceShowEnjoymentDialog(DevActivity.this);
			}
		});
		Button ratingsButton = (Button) findViewById(R.id.button_ratings);
		ratingsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				DevDebugHelper.showRatingDialog(DevActivity.this);
			}
		});
		Button messageCenterButton = (Button) findViewById(R.id.button_message_center);
		messageCenterButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Apptentive.showMessageCenter(DevActivity.this);
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
					surveyModule.show(DevActivity.this, new OnSurveyFinishedListener() {
						public void onSurveyFinished(boolean completed) {
							Log.e(LOG_TAG, "A survey finished, and was " + (completed ? "completed" : "skipped"));
						}
					});
					showSurveyButton.setEnabled(false);
				}
			}
		});

		// If you would like to be notified when there are unread messages available, set a listener like this.
		Apptentive.setUnreadMessagesListener(new UnreadMessagesListener() {
			public void onUnreadMessageCountChanged(final int unreadMessages) {
				Log.e(LOG_TAG, "There are " + unreadMessages + " unread messages.");
				DevActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Button messageCenterButton = (Button) findViewById(R.id.button_message_center);
						messageCenterButton.setText("Message Center, unread = " + unreadMessages);
					}
				});

			}
		});
	}

	// Call the ratings flow. This is one way to do it: Show the ratings flow if conditions are met when the window
	// gains focus.
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			Apptentive.showRatingFlowIfConditionsAreMet(DevActivity.this);
		}
	}
}
