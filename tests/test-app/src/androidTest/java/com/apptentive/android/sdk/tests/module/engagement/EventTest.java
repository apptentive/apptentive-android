/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import androidx.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;
import com.apptentive.android.sdk.util.Util;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class EventTest extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator;

	@Test
	public void eventLabelCreation() {
		BufferedReader reader = null;
		try {
			reader = openBufferedReaderFromFileAsset(TEST_DATA_DIR + "testEventLabelCreation.txt");

			// Make sure the test file isn't empty.
			reader.mark(Integer.MAX_VALUE);
			assertNotNull(reader.readLine());
			reader.reset();

			String vendor;
			while ((vendor = reader.readLine()) != null) {
				String interaction = reader.readLine();
				String eventName = reader.readLine();
				String expected = reader.readLine();
				String result = EngagementModule.generateEventLabel(vendor, interaction, eventName);
				ApptentiveLog.i(".\nexpected: %s\nresult:   %s", expected, result);
				assertTrue(result.equals(expected));
			}
		} catch (IOException e) {
			ApptentiveLog.e(e, "Error reading asset.");
			throw new RuntimeException(e);
		} finally {
			Util.ensureClosed(reader);
		}
	}
}