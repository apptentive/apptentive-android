/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import android.test.InstrumentationTestCase;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.FileUtil;
import com.apptentive.android.sdk.util.JsonDiffer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * @author Sky Kelsey
 */
public class ObjectDiffingTests extends InstrumentationTestCase {

	private static final String TEST_DATA_DIR = "util" + File.separator;

	/**
	 * Tests to make sure that objects that differ are calculated correctly.
 	 */
	public void testDeviceDiffing1() {
		Log.e("testDeviceDiffing1()");
		try {
			JSONObject original = new JSONObject(FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testJsonDiffing.1.old.json"));
			JSONObject updated = new JSONObject(FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testJsonDiffing.1.new.json"));
			JSONObject expected = new JSONObject(FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testJsonDiffing.1.expected.json"));

			JSONObject result = JsonDiffer.getDiff(original, updated);

			Log.e("result: %s", result);
			boolean equal = JsonDiffer.areObjectsEqual(result, expected);
			assertTrue(equal);

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Tests to make sure that objects that are the same return a null diff.
	 */
	public void testDeviceDiffing2() {
		Log.e("testDeviceDiffing2()");
		try {
			JSONObject original = new JSONObject(FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testJsonDiffing.2.old.json"));
			JSONObject updated = new JSONObject(FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testJsonDiffing.2.new.json"));

			JSONObject result = JsonDiffer.getDiff(original, updated);

			Log.e("result: %s", result);
			assertNull(result);

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
