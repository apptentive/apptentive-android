/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.apptentive.android.example.push.RegistrationIntentService;
import com.apptentive.android.sdk.Apptentive;

/**
 * This is an example integration of Apptentive.
 *
 * @author Sky Kelsey
 */
public class ExampleActivity extends Activity {

	private static String LOG_TAG = "Apptentive Example";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Start IntentService to register this application with GCM.
		Intent intent = new Intent(this, RegistrationIntentService.class);
		startService(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Apptentive.onStart(this);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// Only engage if this window is gaining focus.
		if (hasFocus) {
			// If you plan on showing your own views, first make sure Apptentive hasn't already shown one.
			boolean apptentiveShowedInteraction = Apptentive.engage(this, "init");
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Apptentive.onStop(this);
	}

	/**
	 * Provide a simple feedback button in your app.
	 */
	public void onMessageCenterButtonPressed(@SuppressWarnings("unused") View view) {
		Apptentive.showMessageCenter(this);
	}
}
