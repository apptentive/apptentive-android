/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.dev.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.Button;
import com.apptentive.android.dev.TestsActivity;

/**
 * @author Sky Kelsey
 */
public class TestsActivityTest extends ActivityInstrumentationTestCase2<TestsActivity> {

	TestsActivity activity;

	public TestsActivityTest() {
		super("com.apptentive.android.dev", TestsActivity.class);
	}

	public TestsActivityTest(String pkg) {
		super(pkg, TestsActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setActivityInitialTouchMode(false);
		this.activity = getActivity();
	}

	@UiThreadTest
	public void testLaunchingTweet() {
		final Button button = (Button) activity.findViewById(com.apptentive.android.dev.R.id.test_tweet);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				button.requestFocus();
				button.performClick();
			}
		});
	}




}
