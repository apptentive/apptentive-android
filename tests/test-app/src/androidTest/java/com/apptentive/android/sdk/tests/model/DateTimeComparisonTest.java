/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.model;

import androidx.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DateTimeComparisonTest extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "model" + File.separator;

	private static final Map<String, Integer> operatorLookup;

	static {
		operatorLookup = new HashMap<String, Integer>();
		operatorLookup.put("=", 0);
		operatorLookup.put("<", -1);
		operatorLookup.put(">", 1);
	}

	@Test
	public void dateTimeComparison() {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "testDateTimeComparison.json");
		try {
			JSONArray experiments = new JSONArray(json);
			for (int i = 0; i < experiments.length(); i++) {
				JSONArray experiment = experiments.getJSONArray(i);

				double left = experiment.optDouble(0);
				double right = experiment.optDouble(2);
				String operator = experiment.getString(1);
				int expected = operatorLookup.get(operator);

				ApptentiveLog.e("Comparing [\"%s\" %s \"%s\"]", left, operator, right);

				Apptentive.DateTime leftDateTime = new Apptentive.DateTime(left);
				Apptentive.DateTime rightDateTime = new Apptentive.DateTime(right);
				int actual = leftDateTime.compareTo(rightDateTime);

				assertEquals(String.format("Comparison of [\"%s\" %s \"%s\"] failed", left, operator, right), expected, actual);
			}
		} catch (JSONException e) {
			ApptentiveLog.e(e, "Error loading experiment results.");
		}
	}
}
