/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(AndroidJUnit4.class)
public class ApptentiveDatabaseHelperTest {

	@Test
	public void testFoo() {
		final Context context = InstrumentationRegistry.getContext();
		final File apptentive = context.getApplicationContext().getDatabasePath("apptentive");
		System.out.println(apptentive);
	}
}