/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.content.Context;
import android.os.Build;
import android.test.InstrumentationTestCase;
import com.apptentive.android.sdk.model.CodePointStore;

/**
 * @author Sky Kelsey
 */
public class ApptentiveInstrumentationTestCase extends InstrumentationTestCase {

	protected Context targetContext;

	static {
		ApptentiveInternal.setMinimumLogLevel(Log.VERBOSE);
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
}
