/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement.criteria;

import androidx.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.ApptentiveLog;
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CornerCases extends ApptentiveTestCaseBase {
	private static final String TEST_DATA_DIR = "engagement" + File.separator + "criteria" + File.separator;

	@Test
	public void cornerCasesThatShouldBeTrue() throws JSONException {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "testCornerCasesThatShouldBeTrue.json");
		InteractionCriteria criteria = new InteractionCriteria(json);

		EventData eventData = new EventData();
		Device device = new Device();
		device.getCustomData().put("key_with_null_value", null);
		FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), eventData, new Person(), device, new AppRelease());

		assertNotNull("Criteria was null, but it shouldn't be.", criteria);
		assertTrue(criteria.isMet(fieldManager));
		ApptentiveLog.e("Finished test.");
	}

	@Test
	public void cornerCasesThatShouldBeFalse() throws JSONException {
		ApptentiveLog.e("Running test: testCornerCasesThatShouldBeFalse()\n\n");

		String json = loadTextAssetAsString(TEST_DATA_DIR + "testCornerCasesThatShouldBeFalse.json");
		InteractionCriteria criteria = new InteractionCriteria(json);

		EventData eventData = new EventData();
		Device device = new Device();
		device.getCustomData().put("key_with_null_value", null);
		FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), eventData, new Person(), device, new AppRelease());

		assertNotNull("Criteria was null, but it shouldn't be.", criteria);
		assertTrue(criteria.isMet(fieldManager));
		ApptentiveLog.e("Finished test.");
	}
}
