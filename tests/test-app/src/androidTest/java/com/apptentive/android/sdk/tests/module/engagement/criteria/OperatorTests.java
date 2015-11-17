/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement.criteria;

import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionCriteria;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.storage.PersonManager;

import org.json.JSONException;

import java.io.File;

/**
 * @author Sky Kelsey
 */
public class OperatorTests extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator + "criteria" + File.separator;

	public void testOperatorExists() {
		Log.e("Running test: testOperatorExists()\n\n");
		resetDevice();
		doTest("testOperatorExists.json");
		Log.e("Finished test.");
	}

	public void testOperatorNot() {
		Log.e("Running test: testOperatorNot()\n\n");
		resetDevice();
		doTest("testOperatorNot.json");
		Log.e("Finished test.");
	}

	public void testOperatorContains() {
		Log.e("Running test: testOperatorContains()\n\n");
		resetDevice();
		PersonManager.storePersonEmail(getTargetContext(), "test@example.com");
		PersonManager.storePersonAndReturnIt(getTargetContext());
		doTest("testOperatorContains.json");
		Log.e("Finished test.");
	}

	public void testOperatorStartsWith() {
		Log.e("Running test: testOperatorStartsWith()\n\n");
		resetDevice();
		PersonManager.storePersonEmail(getTargetContext(), "test@example.com");
		PersonManager.storePersonAndReturnIt(getTargetContext());
		doTest("testOperatorStartsWith.json");
		Log.e("Finished test.");
	}

	public void testOperatorEndsWith() {
		Log.e("Running test: testOperatorEndsWith()\n\n");
		resetDevice();
		PersonManager.storePersonEmail(getTargetContext(), "test@example.com");
		PersonManager.storePersonAndReturnIt(getTargetContext());
		doTest("testOperatorEndsWith.json");
		Log.e("Finished test.");
	}

	private void doTest(String testFile) {
		String json = loadFileAssetAsString(TEST_DATA_DIR + testFile);
		try {
			InteractionCriteria criteria = new InteractionCriteria(json);
			assertTrue(criteria.isMet(getTargetContext()));
		} catch (JSONException e) {
			Log.e("Error parsing test JSON.", e);
			assertNull(e);
		}
	}
}
