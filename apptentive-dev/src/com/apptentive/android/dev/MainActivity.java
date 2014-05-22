/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.dev;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.apptentive.android.sdk.*;
import com.apptentive.android.sdk.module.messagecenter.UnreadMessagesListener;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;

/**
 * @author Sky Kelsey
 */
public class MainActivity extends ApptentiveActivity {

	public static final String LOG_TAG = "Apptentive Dev App";

	private long lastUnreadMessageCount = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// OPTIONAL: To specify a different user email than what the device was setup with.
		//Apptentive.setUserEmail("user_email@example.com");

		// OPTIONAL: To send extra about the device to the server.
		//Apptentive.addCustomDeviceData(this, "custom_device_key", "custom_device_value");
		//Apptentive.addCustomPersonData(this, "custom_person_key", "custom_person_value");

		// OPTIONAL: Specify a different rating provider if your app is not served from Google Play.
		//Apptentive.setRatingProvider(new AmazonAppstoreRatingProvider());

		// Impersonate an app for ratings.
		//Apptentive.putRatingProviderArg("package", "your.package.name");

		// If you would like to be notified when there are unread messages available, set a listener like this.
		Apptentive.setUnreadMessagesListener(new UnreadMessagesListener() {
			public void onUnreadMessageCountChanged(final int unreadMessages) {
				Log.e(LOG_TAG, "There are " + unreadMessages + " unread messages.");
				runOnUiThread(new Runnable() {
					public void run() {
						Button messageCenterButton = (Button) findViewById(R.id.button_message_center);
						if (messageCenterButton != null) {
							messageCenterButton.setText("Message Center, unread = " + unreadMessages);
						}
						if (lastUnreadMessageCount < unreadMessages) {
							Toast.makeText(MainActivity.this, "You have " + unreadMessages + " unread messages.", Toast.LENGTH_SHORT).show();
						}
					}
				});
				lastUnreadMessageCount = unreadMessages;
			}
		});

		// Ad a listener to notify you when a survey is completed.
		Apptentive.setOnSurveyFinishedListener(new OnSurveyFinishedListener() {
			@Override
			public void onSurveyFinished(boolean completed) {
				Toast.makeText(MainActivity.this, completed ? "Survey was completed." : "Survey was skipped.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void launchInteractionsActivity(@SuppressWarnings("unused") View view) {
		startActivity(new Intent(this, InteractionsActivity.class));
	}

	public void launchMessageCenterActivity(@SuppressWarnings("unused") View view) {
		startActivity(new Intent(this, MessageCenterActivity.class));
	}

	public void launchTestsActivity(@SuppressWarnings("unused") View view) {
		startActivity(new Intent(this, TestsActivity.class));
	}

	public void launchDataActivity(@SuppressWarnings("unused") View view) {
		startActivity(new Intent(this, DataActivity.class));
	}

	public void showMessageCenter(@SuppressWarnings("unused") View view) {
		Apptentive.showMessageCenter(this);
	}

	// Call the ratings flow. This is one way to do it: Show the ratings flow if conditions are met when the window
	// gains focus.
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			boolean ret = Apptentive.engage(this, "init");
			Log.e(LOG_TAG, "Rating flow " + (ret ? "was" : "was not") + " shown.");
		}
	}
}
