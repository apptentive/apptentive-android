/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.os.SystemClock;

import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.threading.MockDispatchQueue;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TestCaseBase {

	private List<String> result = new ArrayList<>();
	private MockDispatchQueue dispatchQueue;

	//region Results

	protected void addResult(String str) {
		result.add(str);
	}

	protected void assertResult(String... expected) {
		// Make sure the expected and result sets contain the same number of items
		if (expected.length != result.size()) {
			fail(String.format("Expected: [%s], Actual: [%s]", StringUtils.join(expected), StringUtils.join(result)));
		}

		// Make sure the order and values are the same as well
		for (int i = 0; i < expected.length; ++i) {
			assertEquals(String.format("Expected: [%s], Actual: [%s],", StringUtils.join(expected), StringUtils.join(result)), expected[i], result.get(i));
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
