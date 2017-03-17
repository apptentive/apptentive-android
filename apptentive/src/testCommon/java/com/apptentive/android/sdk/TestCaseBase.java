/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.os.SystemClock;

import com.apptentive.android.sdk.debug.Assert;
import com.apptentive.android.sdk.debug.AssertImp;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.threading.MockDispatchQueue;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TestCaseBase {

	private List<String> result = new ArrayList<>();
	private MockDispatchQueue dispatchQueue;

	//region Setup

	protected void setUp() {
		Assert.setImp(new AssertImp() {
			@Override
			public void assertFailed(String message) {
				throw new AssertionError(message);
			}
		});
	}

	protected void tearDown() {
		Assert.setImp(null);
	}

	//endregion

	//region Results

	protected void addResult(String str) {
		result.add(str);
	}

	protected void assertResult(String... expected) {
		assertEquals("\nExpected: " + StringUtils.join(expected) +
			"\nActual: " + StringUtils.join(result), expected.length, result.size());

		for (int i = 0; i < expected.length; ++i) {
			assertEquals("\nExpected: " + StringUtils.join(expected) +
					"\nActual: " + StringUtils.join(result),
				expected[i], result.get(i));
		}

		result.clear();
	}
	//endregion

	//region Dispatch Queue

	protected void overrideMainQueue(boolean runImmediately) {
		dispatchQueue = MockDispatchQueue.overrideMainQueue(runImmediately);
	}

	protected void dispatchTasks() {
		dispatchQueue.dispatchTasks();
	}

	//endregion

	//region Helpers

	protected void sleep(long millis) {
		SystemClock.sleep(millis);
	}

	//endregion
}
