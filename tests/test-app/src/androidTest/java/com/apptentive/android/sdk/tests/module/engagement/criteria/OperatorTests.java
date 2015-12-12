/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement.criteria;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionCriteria;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.Log;

import org.json.JSONException;

import java.io.File;

/**
 * @author Sky Kelsey
 */
public class OperatorTests extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator + "criteria" + File.separator;

	public void testOperatorExists() {
		Log.e("Running test: testOperatorExists()\n\n");
		doTest("testOperatorExists.json");
		Log.e("Finished test.");
	}

	public void testOperatorNot() {
		Log.e("Running test: testOperatorNot()\n\n");
		doTest("testOperatorNot.json");
		Log.e("Finished test.");
	}

	public void testOperatorLessThan() {
		Log.e("Running test: testOperatorLessThan()\n\n");
		doTest("testOperatorLessThan.json");
		Log.e("Finished test.");
	}

	public void testOperatorLessThanOrEqual() {
		Log.e("Running test: testOperatorLessThanOrEqual()\n\n");
		doTest("testOperatorLessThanOrEqual.json");
		Log.e("Finished test.");
	}

	public void testOperatorGreaterThanOrEqual() {
		Log.e("Running test: testOperatorGreaterThanOrEqual()\n\n");
		doTest("testOperatorGreaterThanOrEqual.json");
		Log.e("Finished test.");
	}

	public void testOperatorGreaterThan() {
		Log.e("Running test: testOperatorGreaterThan()\n\n");
		doTest("testOperatorGreaterThan.json");
		Log.e("Finished test.");
	}

	public void testOperatorContains() {
		Log.e("Running test: testOperatorContains()\n\n");
		doTest("testOperatorContains.json");
		Log.e("Finished test.");
	}

	public void testOperatorStartsWith() {
		Log.e("Running test: testOperatorStartsWith()\n\n");
		doTest("testOperatorStartsWith.json");
		Log.e("Finished test.");
	}

	public void testOperatorEndsWith() {
		Log.e("Running test: testOperatorEndsWith()\n\n");
		doTest("testOperatorEndsWith.json");
		Log.e("Finished test.");
	}

	public void testOperatorBefore() {
		Log.e("Running test: testOperatorBefore()\n\n");
		doTest("testOperatorBefore.json");
		Log.e("Finished test.");
	}

	public void testOperatorAfter() {
		Log.e("Running test: testOperatorAfter()\n\n");
		doTest("testOperatorAfter.json");
		Log.e("Finished test.");
	}

	private void doTest(String testFile) {
		String json = loadFileAssetAsString(TEST_DATA_DIR + testFile);
		try {
			Apptentive.addCustomDeviceData(getTargetContext(), "number_5", 5);
			Apptentive.addCustomDeviceData(getTargetContext(), "string_qwerty", "qwerty");
			Apptentive.addCustomDeviceData(getTargetContext(), "boolean_true", true);
			Apptentive.DateTime dateTime = new Apptentive.DateTime(1000d);
			//Apptentive.addCustomDeviceData(getTargetContext(), "datetime_1000", dateTime);
			ApptentiveInternal.addCustomDeviceData(getTargetContext(), "datetime_1000", dateTime);
			Apptentive.Version version = new Apptentive.Version();
			version.setVersion("1.2.3");
			//Apptentive.addCustomDeviceData(getTargetContext(), "version_1.2.3", version);
			ApptentiveInternal.addCustomDeviceData(getTargetContext(), "version_1.2.3", version);
			Apptentive.addCustomDeviceData(getTargetContext(), "key_with_null_value", (String) null);

			DeviceManager.storeDeviceAndReturnIt(getTargetContext());
			InteractionCriteria criteria = new InteractionCriteria(json);
			assertTrue(criteria.isMet(getTargetContext()));
		} catch (JSONException e) {
			Log.e("Error parsing test JSON.", e);
			assertNull(e);
		}
	}
}
