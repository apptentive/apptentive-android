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
public class VersionComparisonTest extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "model" + File.separator;

	private static final Map<String, Integer> operatorLookup;

	static {
		operatorLookup = new HashMap<String, Integer>();
		operatorLookup.put("=", 0);
		operatorLookup.put("<", -1);
		operatorLookup.put(">", 1);
	}

	@Test
	public void basicVersionComparison() {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "testBasicVersionComparison.json");

		try {
			JSONArray experiments = new JSONArray(json);
			for (int i = 0; i < experiments.length(); i++) {
				JSONArray experiment = experiments.getJSONArray(i);

				Object left = experiment.get(0);
				if (!(left instanceof String)) {
					left = experiment.getLong(0);
				}
				Object right = experiment.get(2);
				if (!(right instanceof String)) {
					right = experiment.getLong(2);
				}
				String operator = experiment.getString(1);
				int expected = operatorLookup.get(operator);

				Apptentive.Version leftVersion;
				if (left instanceof String) {
					leftVersion = new Apptentive.Version();
					leftVersion.setVersion((String) left);
				} else {
					leftVersion = new Apptentive.Version((Long) left);
				}
				Apptentive.Version rightVersion;
				if (right instanceof String) {
					rightVersion = new Apptentive.Version();
					rightVersion.setVersion((String) right);
				} else {
					rightVersion = new Apptentive.Version((Long) right);
				}
				int actual = leftVersion.compareTo(rightVersion);

				assertEquals(String.format("Comparison of [\"%s\" %s \"%s\"] failed", left, operator, right), expected, actual);
			}
		} catch (JSONException e) {
			ApptentiveLog.e(e, "Error loading experiment results.");
		}
	}
}
