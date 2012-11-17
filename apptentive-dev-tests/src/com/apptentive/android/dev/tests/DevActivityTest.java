package com.apptentive.android.dev.tests;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import com.apptentive.android.dev.DevActivity;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.apptentive.android.dev.tests.DevActivityTest \
 * com.apptentive.android.dev.tests/android.test.InstrumentationTestRunner
 */
public class DevActivityTest extends ActivityInstrumentationTestCase2<DevActivity> {

	DevActivity activity;

	public DevActivityTest() {
		super("com.apptentive.android.dev", DevActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setActivityInitialTouchMode(false);
		activity = getActivity();
	}

	public void testUsesLifecycle() {
	}
}
