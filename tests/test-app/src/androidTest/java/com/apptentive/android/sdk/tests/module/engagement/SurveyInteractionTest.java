/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import androidx.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.SurveyInteraction;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class SurveyInteractionTest extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator;

	@Test
	public void surveyParsing() {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "testSurveyParsing.json");
		Interaction survey = null;
		try {
			survey = new SurveyInteraction(json);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Error loading survey.");
		}
		assertNotNull(survey);
	}
}
