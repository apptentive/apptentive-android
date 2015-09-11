/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.dev;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.apptentive.android.dev.push.RegistrationIntentService;
import com.apptentive.android.sdk.*;
import com.apptentive.android.sdk.module.messagecenter.UnreadMessagesListener;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;

/**
 * @author Sky Kelsey
 */
public class MainActivity extends ApptentiveActivity {

	private UnreadMessagesListener unreadMessagesListener;
	private OnSurveyFinishedListener surveyFinishedListener;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// Log from Application when number of unread messages changes.
		if (unreadMessagesListener == null) {
			Log.e("Adding UnreadMessagesListener");
			unreadMessagesListener = new UnreadMessagesListener() {
				public void onUnreadMessageCountChanged(final int unreadMessages) {
					Log.e("Unread message count changed. There are %s unread messages.", unreadMessages);
				}
			};
			Apptentive.addUnreadMessagesListener(unreadMessagesListener);
		}

		// Log in Application when a survey is completed.
		if (surveyFinishedListener == null) {
			surveyFinishedListener = new OnSurveyFinishedListener() {
				@Override
				public void onSurveyFinished(boolean completed) {
					Log.e(completed ? "Survey was completed." : "Survey was skipped.");
				}
			};
			Apptentive.setOnSurveyFinishedListener(surveyFinishedListener);
		}

		// Catch all Exceptions during development and log exception and causes.
		Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				Log.e("UncaughtException", e);
				while (e.getCause() != null) {
					e = e.getCause();
					Log.e("Caused by:", e);
				}
			}
		});

		// GCM: Start IntentService to register this application.
		Intent intent = new Intent(this, RegistrationIntentService.class);
		startService(intent);
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

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			Apptentive.handleOpenedPushNotification(this);
		}
	}
}
