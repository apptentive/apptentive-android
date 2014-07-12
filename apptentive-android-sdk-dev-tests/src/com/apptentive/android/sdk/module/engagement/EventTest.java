/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import com.apptentive.android.sdk.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.FileUtil;
import com.apptentive.android.sdk.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

/**
 * @author Sky Kelsey
 */
public class EventTest extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator;

	public void testEventLabelCreation() {
		Log.e("Running test: testEventLabelCreation()\n\n");

		BufferedReader reader = null;
		try {
			reader = FileUtil.openBufferedReaderFromFileAsset(getInstrumentation().getContext(), TEST_DATA_DIR + "testEventLabelCreation.txt");

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
				Log.i(".\nexpected: %s\nresult:   %s", expected, result);
				assertTrue(result.equals(expected));
			}
		} catch (IOException e) {
			Log.e("Error reading asset.", e);
			throw new RuntimeException(e);
		} finally {
			Util.ensureClosed(reader);
		}
	}
}