/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests;

import android.content.Context;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.test.RenamingDelegatingContext;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;

import org.junit.Before;

/**
 * Initializes the Apptentive SDK so tests can work.
 */
public abstract class ApptentiveTestCaseBase {
	protected Context targetContext;
	protected ApptentiveInternal apptentiveInternal;
	protected InteractionManager interactionManager;
	protected CodePointStore codePointStore;

	@Before
	public void initializeApptentiveSdk() {
		targetContext = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
		apptentiveInternal = ApptentiveInternal.getInstance(targetContext);
		apptentiveInternal.setMinimumLogLevel(ApptentiveLog.Level.VERBOSE);
		interactionManager = apptentiveInternal.getInteractionManager();
		codePointStore = apptentiveInternal.getCodePointStore();
	}

	protected void resetDevice() {
		targetContext.getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE).edit().clear().commit();
		apptentiveInternal.getCodePointStore().clear();
		apptentiveInternal.getInteractionManager().clear();

	}

	protected static boolean isRunningOnEmulator() {
		return Build.FINGERPRINT.contains("generic");
	}
}
