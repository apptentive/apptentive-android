/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import android.support.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.logic.Clause;
import com.apptentive.android.sdk.module.engagement.logic.ClauseParser;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class CriteriaParsingTest extends ApptentiveTestCaseBase {
	private static final String TEST_DATA_DIR = "engagement" + File.separator + "criteria" + File.separator;

	@Test
	public void predicateParsing() throws JSONException {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "testPredicateParsing.json");

		try {
			Clause criteria = ClauseParser.parse(json);
			assertNotNull("Criteria was null, but it shouldn't be.", criteria);
		} catch (JSONException e) {
			ApptentiveLog.e(e, "Error parsing test JSON.");
			assertNotNull(e);
		}
	}
}
