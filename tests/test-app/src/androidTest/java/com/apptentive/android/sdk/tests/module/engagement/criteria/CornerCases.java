/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement.criteria;

import android.support.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.logic.Clause;
import com.apptentive.android.sdk.module.engagement.logic.ClauseParser;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CornerCases extends ApptentiveTestCaseBase {
	private static final String TEST_DATA_DIR = "engagement" + File.separator + "criteria" + File.separator;

	@Test
	public void ornerCasesThatShouldBeTrue() throws JSONException {
		resetDevice();

		String json = loadTextAssetAsString(TEST_DATA_DIR + "testCornerCasesThatShouldBeTrue.json");
		Apptentive.addCustomDeviceData("key_with_null_value", (String) null);
		DeviceManager.storeDeviceAndReturnIt();
		try {
			Clause criteria = ClauseParser.parse(json);
			assertNotNull("Criteria was null, but it shouldn't be.", criteria);
			boolean result = criteria.evaluate();
			assertTrue(result);
		} catch (JSONException e) {
			ApptentiveLog.e(e, "Error parsing test JSON.");
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}

	@Test
	public void cornerCasesThatShouldBeFalse() throws JSONException {
		ApptentiveLog.e("Running test: testCornerCasesThatShouldBeFalse()\n\n");
		resetDevice();

		String json = loadTextAssetAsString(TEST_DATA_DIR + "testCornerCasesThatShouldBeFalse.json");
		Apptentive.addCustomDeviceData("key_with_null_value", (String) null);
		DeviceManager.storeDeviceAndReturnIt();
		try {
			Clause criteria = ClauseParser.parse(json);
			assertNotNull("Criteria was null, but it shouldn't be.", criteria);
			boolean result = criteria.evaluate();
			assertTrue(result);
		} catch (JSONException e) {
			ApptentiveLog.e(e, "Error parsing test JSON.");
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}
}
