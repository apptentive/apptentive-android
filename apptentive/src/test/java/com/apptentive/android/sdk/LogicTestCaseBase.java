/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import com.apptentive.android.sdk.util.RuntimeUtils;
import com.apptentive.android.sdk.util.threading.MockDispatchQueue;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogicTestCaseBase extends TestCaseBase {

	//region Setup

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		try {
			// main queue
			overrideMainQueue(true);

			// conversation queue
			RuntimeUtils.overrideStaticFinalField(findHolderClass(ApptentiveHelper.class.getDeclaredClasses()), "CONVERSATION_QUEUE", new MockDispatchQueue(true));
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	private Class<?> findHolderClass(Class<?>[] classes) {
		for (Class<?> cls : classes) {
			if (cls.getSimpleName().equals("Holder")) {
				return cls;
			}
		}
		return null;
	}

	//endregion
}
