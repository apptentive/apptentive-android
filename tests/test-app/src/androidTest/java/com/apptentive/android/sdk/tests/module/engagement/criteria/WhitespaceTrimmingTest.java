/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement.criteria;

import android.support.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionCriteria;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class WhitespaceTrimmingTest extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator + "criteria" + File.separator;

	@Test
	public void whitespaceTrimming() {
		doTest("testWhitespaceTrimming.json");
	}

	private void doTest(String testFile) {
		String json = loadTextAssetAsString(TEST_DATA_DIR + testFile);
		try {
			Apptentive.addCustomDeviceData(" string_qwerty ", " qwerty ");
			Apptentive.addCustomDeviceData(" string with spaces ", " string with spaces ");
			DeviceManager.storeDeviceAndReturnIt();
			InteractionCriteria criteria = new InteractionCriteria(json);
			assertTrue(criteria.isMet());
		} catch (JSONException e) {
			ApptentiveLog.e(e, "Error parsing test JSON.");
			assertNull(e);
		}
	}
}
