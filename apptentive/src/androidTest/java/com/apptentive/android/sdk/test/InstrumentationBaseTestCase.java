/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.test;

import android.os.SystemClock;

import com.apptentive.android.sdk.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by alementuev on 2/2/17.
 */

public class InstrumentationBaseTestCase {
	private List<String> result = new ArrayList<>();

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

	//region Helpers

	protected void sleep(long millis) {
		SystemClock.sleep(millis);
	}

	//endregion
}
