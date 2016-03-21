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
import com.apptentive.android.sdk.ApptentiveLog;

import org.json.JSONException;

import java.io.File;

/**
 * @author Sky Kelsey
 */
public class OperatorTests extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator + "criteria" + File.separator;

	public void testOperatorExists() {
		ApptentiveLog.e("Running test: testOperatorExists()\n\n");
		doTest("testOperatorExists.json");
		ApptentiveLog.e("Finished test.");
	}

	public void testOperatorNot() {
		ApptentiveLog.e("Running test: testOperatorNot()\n\n");
		doTest("testOperatorNot.json");
		ApptentiveLog.e("Finished test.");
	}

	public void testOperatorLessThan() {
		ApptentiveLog.e("Running test: testOperatorLessThan()\n\n");
		doTest("testOperatorLessThan.json");
		ApptentiveLog.e("Finished test.");
	}

	public void testOperatorLessThanOrEqual() {
		ApptentiveLog.e("Running test: testOperatorLessThanOrEqual()\n\n");
		doTest("testOperatorLessThanOrEqual.json");
		ApptentiveLog.e("Finished test.");
	}

	public void testOperatorGreaterThanOrEqual() {
		ApptentiveLog.e("Running test: testOperatorGreaterThanOrEqual()\n\n");
		doTest("testOperatorGreaterThanOrEqual.json");
		ApptentiveLog.e("Finished test.");
	}

	public void testOperatorGreaterThan() {
		ApptentiveLog.e("Running test: testOperatorGreaterThan()\n\n");
		doTest("testOperatorGreaterThan.json");
		ApptentiveLog.e("Finished test.");
	}

	public void testOperatorContains() {
		ApptentiveLog.e("Running test: testOperatorContains()\n\n");
		doTest("testOperatorContains.json");
		ApptentiveLog.e("Finished test.");
	}

	public void testOperatorStartsWith() {
		ApptentiveLog.e("Running test: testOperatorStartsWith()\n\n");
		doTest("testOperatorStartsWith.json");
		ApptentiveLog.e("Finished test.");
	}

	public void testOperatorEndsWith() {
		ApptentiveLog.e("Running test: testOperatorEndsWith()\n\n");
		doTest("testOperatorEndsWith.json");
		ApptentiveLog.e("Finished test.");
	}

	public void testOperatorBefore() {
		ApptentiveLog.e("Running test: testOperatorBefore()\n\n");
		doTest("testOperatorBefore.json");
		ApptentiveLog.e("Finished test.");
	}

	public void testOperatorAfter() {
		ApptentiveLog.e("Running test: testOperatorAfter()\n\n");
		doTest("testOperatorAfter.json");
		ApptentiveLog.e("Finished test.");
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
			ApptentiveLog.e("Error parsing test JSON.", e);
			assertNull(e);
		}
	}
}
