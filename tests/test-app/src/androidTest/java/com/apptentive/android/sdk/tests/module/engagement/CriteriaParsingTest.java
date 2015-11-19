/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.engagement.logic.Clause;
import com.apptentive.android.sdk.module.engagement.logic.ClauseParser;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.tests.util.FileUtil;

import org.json.JSONException;

import java.io.File;

/**
 * @author Sky Kelsey
 */
public class CriteriaParsingTest extends ApptentiveInstrumentationTestCase {
	private static final String TEST_DATA_DIR = "engagement" + File.separator + "criteria" + File.separator;

	public void testPredicateParsing() throws JSONException {
		Log.e("Running test: testPredicateParsing()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "testPredicateParsing.json");

		try {
			Clause criteria = ClauseParser.parse(json);
			assertNotNull("Criteria was null, but it shouldn't be.", criteria);
		} catch (JSONException e) {
			Log.e("Error parsing test JSON.", e);
			assertNotNull(e);
		}
		Log.e("Finished test.");
	}
}
