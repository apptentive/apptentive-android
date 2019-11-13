/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.model;

import androidx.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;
import com.apptentive.android.sdk.util.JsonDiffer;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ObjectDiffingTests extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "util" + File.separator;

	/**
	 * Tests to make sure that objects that differ are calculated correctly.
	 */
	@Test
	public void deviceDiffing1() {
		try {
			JSONObject original = new JSONObject(loadTextAssetAsString(TEST_DATA_DIR + "testJsonDiffing.1.old.json"));
			JSONObject updated = new JSONObject(loadTextAssetAsString(TEST_DATA_DIR + "testJsonDiffing.1.new.json"));
			JSONObject expected = new JSONObject(loadTextAssetAsString(TEST_DATA_DIR + "testJsonDiffing.1.expected.json"));

			JSONObject result = JsonDiffer.getDiff(original, updated);

			ApptentiveLog.e("result: %s", result);
			boolean equal = JsonDiffer.areObjectsEqual(result, expected);
			assertTrue(equal);

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Tests to make sure that objects that are the same return a null diff.
	 */
	@Test
	public void deviceDiffing2() {
		try {
			JSONObject original = new JSONObject(loadTextAssetAsString(TEST_DATA_DIR + "testJsonDiffing.2.old.json"));
			JSONObject updated = new JSONObject(loadTextAssetAsString(TEST_DATA_DIR + "testJsonDiffing.2.new.json"));

			JSONObject result = JsonDiffer.getDiff(original, updated);

			ApptentiveLog.e("result: %s", result);
			assertNull(result);

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
