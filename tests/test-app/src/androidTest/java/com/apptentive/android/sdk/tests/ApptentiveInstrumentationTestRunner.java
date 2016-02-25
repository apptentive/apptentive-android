
/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests;
import android.test.AndroidTestRunner;
import android.test.InstrumentationTestRunner;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
/**
 * Test runner for {@link ApptentiveInstrumentationTestCase}s. Such tests are executed
 * on the device and have access to an applications context.
 */
public class ApptentiveInstrumentationTestRunner extends InstrumentationTestRunner {

	@Override
	protected AndroidTestRunner getAndroidTestRunner() {
		AndroidTestRunner testRunner = super.getAndroidTestRunner();
		testRunner.addTestListener(new TestListener() {
			@Override
			public void startTest(Test test) {
				if (test instanceof ApptentiveInstrumentationTestCase) {
					((ApptentiveInstrumentationTestCase)test).initialize(null);
				}
			}
			@Override
			public void endTest(Test test) {
			}
			@Override
			public void addFailure(Test test, AssertionFailedError e) {
			}
			@Override
			public void addError(Test test, Throwable t) {
			}
		});
		return testRunner;
	}
}