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
	protected void setUp() {
		super.setUp();

		// logger
		try {
			RuntimeUtils.overrideStaticFinalField(ApptentiveLog.class, "LOGGER_IMPLEMENTATION", new ApptentiveLog.LoggerImplementation() {
				@Override
				public void println(int priority, String tag, String msg) {
					System.out.println(tag + "/" + msg);
				}

				@Override
				public String getStackTraceString(Throwable throwable) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					throwable.printStackTrace(pw);
					return sw.toString();
				}
			});

			// main queue
			overrideMainQueue(true);

			// conversation queue
			RuntimeUtils.overrideStaticFinalField(findHolderClass(ApptentiveHelper.class.getDeclaredClasses()), "INSTANCE", new MockDispatchQueue(true));
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
