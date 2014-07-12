/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import com.apptentive.android.dev.util.FileUtil;
import com.apptentive.android.sdk.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.SurveyInteraction;

import java.io.File;

/**
 * @author Sky Kelsey
 */
public class SurveyInteractionTest extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator;

	public void testSurveyParsing() {
		Log.e("Running test: testSurveyParsing()\n\n");
		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testSurveyParsing.json");
		Interaction survey = null;
		try {
			survey = new SurveyInteraction(json);
		} catch (Exception e) {
			Log.e("Error loading survey.", e);
		}
		assertNotNull(survey);
	}
}
