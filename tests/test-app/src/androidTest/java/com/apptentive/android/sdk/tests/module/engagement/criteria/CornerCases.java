/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement.criteria;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.engagement.logic.Clause;
import com.apptentive.android.sdk.module.engagement.logic.ClauseParser;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.tests.util.FileUtil;

import org.json.JSONException;

import java.io.File;

/**
 * @author Sky Kelsey
 */
public class CornerCases extends ApptentiveInstrumentationTestCase {
	private static final String TEST_DATA_DIR = "engagement" + File.separator + "criteria" + File.separator;

	public void testCornerCasesThatShouldBeTrue() throws JSONException {
		Log.e("Running test: testCornerCasesThatShouldBeTrue()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "testCornerCasesThatShouldBeTrue.json");
		Apptentive.addCustomDeviceData(getTargetContext(), "key_with_null_value", (String)null);
		DeviceManager.storeDeviceAndReturnIt(getTargetContext());
		try {
			Clause criteria = ClauseParser.parse(json);
			assertNotNull("Criteria was null, but it shouldn't be.", criteria);
			boolean result = criteria.evaluate(getTargetContext());
			assertTrue(result);
		} catch (JSONException e) {
			Log.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		Log.e("Finished test.");
	}

	public void testCornerCasesThatShouldBeFalse() throws JSONException {
		Log.e("Running test: testCornerCasesThatShouldBeFalse()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "testCornerCasesThatShouldBeFalse.json");
		Apptentive.addCustomDeviceData(getTargetContext(), "key_with_null_value", (String) null);
		DeviceManager.storeDeviceAndReturnIt(getTargetContext());
		try {
			Clause criteria = ClauseParser.parse(json);
			assertNotNull("Criteria was null, but it shouldn't be.", criteria);
			boolean result = criteria.evaluate(getTargetContext());
			assertTrue(result);
		} catch (JSONException e) {
			Log.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		Log.e("Finished test.");
	}

}
