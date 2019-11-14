/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.content.Context;
import androidx.test.InstrumentationRegistry;

public class InstrumentationTestCaseBase extends TestCaseBase {
	protected Context getContext() {
		return InstrumentationRegistry.getTargetContext();
	}
}
