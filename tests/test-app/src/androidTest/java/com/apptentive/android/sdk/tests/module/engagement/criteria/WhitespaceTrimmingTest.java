/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement.criteria;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionCriteria;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;

import org.json.JSONException;

import java.io.File;

/**
 * @author Sky Kelsey
 */
public class WhitespaceTrimmingTest extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator + "criteria" + File.separator;

	public void testWhitespaceTrimming() {
		Log.e("Running test: testWhitespaceTrimming()\n\n");
		doTest("testWhitespaceTrimming.json");
		Log.e("Finished test.");
	}

	private void doTest(String testFile) {
		String json = loadFileAssetAsString(TEST_DATA_DIR + testFile);
		try {
			Apptentive.addCustomDeviceData(getTargetContext(), " string_qwerty ", " qwerty ");
			Apptentive.addCustomDeviceData(getTargetContext(), " string with spaces ", " string with spaces ");
			DeviceManager.storeDeviceAndReturnIt(getTargetContext());
			InteractionCriteria criteria = new InteractionCriteria(json);
			assertTrue(criteria.isMet(getTargetContext()));
		} catch (JSONException e) {
			Log.e("Error parsing test JSON.", e);
			assertNull(e);
		}
	}
}
