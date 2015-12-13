/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.model;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.tests.util.FileUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class DateTimeComparisonTest extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "model" + File.separator;

	private static final Map<String, Integer> operatorLookup;

	static {
		operatorLookup = new HashMap<String, Integer>();
		operatorLookup.put("=", 0);
		operatorLookup.put("<", -1);
		operatorLookup.put(">", 1);
	}

	public void testDateTimeComparison() {
		Log.e("Running test: testDateTimeComparison()\n\n");
		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "testDateTimeComparison.json");

		try {
			JSONArray experiments = new JSONArray(json);
			for (int i = 0; i < experiments.length(); i++) {
				JSONArray experiment = experiments.getJSONArray(i);

				double left = experiment.optDouble(0);
				double right = experiment.optDouble(2);
				String operator = experiment.getString(1);
				int expected = operatorLookup.get(operator);

				Log.e("Comparing [\"%s\" %s \"%s\"]", left, operator, right);

				Apptentive.DateTime leftDateTime = new Apptentive.DateTime(left);
				Apptentive.DateTime rightDateTime = new Apptentive.DateTime(right);
				int actual = leftDateTime.compareTo(rightDateTime);

				assertEquals(String.format("Comparison of [\"%s\" %s \"%s\"] failed", left, operator, right), expected, actual);
			}
		} catch (JSONException e) {
			Log.e("Error loading experiment results.", e);
		}

	}
}
