/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.test.InstrumentationTestCase;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.tests.util.FileUtil;

/**
 * @author Sky Kelsey
 */
public class ApptentiveInstrumentationTestCase extends InstrumentationTestCase {

	protected Context testContext;
	protected Context targetContext;


	/**
	 * Initializes this test case.
	 *
	 * @param params Instrumentation arguments.
	 */
	void initialize(Bundle params) {
		ApptentiveInternal.getInstance(getTargetContext()).setMinimumLogLevel(Log.Level.VERBOSE);
	}

	protected Context getTestContext() {
		if (testContext == null) {
			testContext = getInstrumentation().getContext();
		}
		return testContext;
	}

	protected Context getTargetContext() {
		if (targetContext == null) {
			targetContext = getInstrumentation().getTargetContext();
		}
		return targetContext;
	}

	protected void resetDevice() {
		getTargetContext().getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE).edit().clear().commit();
		CodePointStore.clear(getTargetContext());
		ApptentiveInternal.getInteractionManager(getTargetContext()).clear(getTargetContext());
	}

	protected static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

	protected static boolean isRunningOnEmulator() {
		return Build.FINGERPRINT.contains("generic");
	}

	protected String loadFileAssetAsString(String pathFromAssetsDirectory) {
		return FileUtil.loadTextAssetAsString(getTestContext(), pathFromAssetsDirectory);
	}
}
