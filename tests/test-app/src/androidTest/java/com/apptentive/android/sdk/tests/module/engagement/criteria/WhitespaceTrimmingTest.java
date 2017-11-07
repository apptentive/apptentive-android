/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement.criteria;

import android.support.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionCriteria;
import com.apptentive.android.sdk.module.engagement.logic.FieldManager;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.EventData;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.VersionHistory;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class WhitespaceTrimmingTest extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator + "criteria" + File.separator;

	@Test
	public void whitespaceTrimming() throws JSONException {
		doTest("testWhitespaceTrimming.json");
	}

	private void doTest(String testFile) throws JSONException {
		String json = loadTextAssetAsString(TEST_DATA_DIR + testFile);

		InteractionCriteria criteria = new InteractionCriteria(json);

		Device device = new Device();
		FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), new EventData(), new Person(), device, new AppRelease());

		device.getCustomData().put(" string_qwerty ", "qwerty");
		device.getCustomData().put(" string with spaces ", "string with spaces");


		/*
		 TODO: It looks like we weren't trimming custom_data keys before storing them. What is the
		 implication of doing that? We can't store a key under " key " and then retrieve it with "key"
		  */
		assertTrue(criteria.isMet(fieldManager));
	}
}
