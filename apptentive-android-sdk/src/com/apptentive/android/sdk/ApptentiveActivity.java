/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;

/**
 * Extend this class instead of Activity to easily integrate Apptentive into your application.
 * <p/>
 * If you are unable to inherit from our Activity, you can delegate the calls to Apptentive static methods into your
 * own Activity manually, as specified below.
 * <p/>
 * All Activities in your Application MUST integrate Apptentive by extending of of the Apptentive Activities, or by
 * delegation.
 * @see ApptentiveListActivity
 * @author Sky Kelsey
 */
@SuppressLint("Registered")
public class ApptentiveActivity extends Activity {

	@Override
	protected void onStart() {
		super.onStart();
		Apptentive.onStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Apptentive.onStop(this);
	}
}
