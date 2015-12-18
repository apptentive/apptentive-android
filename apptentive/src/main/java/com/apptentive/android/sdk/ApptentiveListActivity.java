/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.annotation.SuppressLint;
import android.app.ListActivity;

/**
 * <p>Extend this class instead of ListActivity to easily integrate Apptentive into your application.</p>
 * <p>If you are unable to inherit from our Activity, you can delegate the calls to Apptentive static methods into your
 * own Activity manually, as specified below.</p>
 * <p>All Activities in your Application MUST integrate Apptentive by extending of of the Apptentive Activities, or by
 * delegation.</p>
 *
 * @author Sky Kelsey
 * @see ApptentiveActivity
 */
@SuppressLint("Registered")
public class ApptentiveListActivity extends ListActivity {

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
