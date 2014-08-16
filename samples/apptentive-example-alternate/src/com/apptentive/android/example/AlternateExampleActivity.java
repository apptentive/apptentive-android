/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.apptentive.android.sdk.Apptentive;

/**
 * This is an example of how to integrate Apptentive into your Application if you are not able to inherit from
 * {@link com.apptentive.android.sdk.ApptentiveActivity}. In this case, you must hook Apptentive up to your Activity's
 * onStart(), and onStop() methods.
 *
 * @author Sky Kelsey
 */
public class AlternateExampleActivity extends Activity {

	private static String LOG_TAG = "Alternate Apptentive Example";

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
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
			Apptentive.engage(this, "init");
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
}
