/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import android.content.Context;
import android.test.InstrumentationTestCase;
import com.apptentive.android.dev.util.FileUtil;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.SurveyInteraction;

import java.io.File;

/**
 * @author Sky Kelsey
 */
public class SurveyInteractionTest extends InstrumentationTestCase {

	private Context context;

	private static final String TEST_DATA_DIR = "engagement" + File.separator;

	private Context getTargetContext() {
		if (context == null) {
			context = getInstrumentation().getTargetContext();
		}
		return context;
	}

	private void resetDevice() {
		getTargetContext().getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE).edit().clear().commit();
		CodePointStore.clear(getTargetContext());
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

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
