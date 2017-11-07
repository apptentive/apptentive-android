/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import android.os.Build;
import android.support.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionCriteria;
import com.apptentive.android.sdk.module.engagement.logic.FieldManager;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.EventData;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.VersionHistory;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DataObjectQueryTest extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "engagement/payloads/";

	@Test
	public void queriesAgainstPerson() throws JSONException {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "testQueriesAgainstPerson.json");
		InteractionCriteria criteria = new InteractionCriteria(json);

		Person person = new Person();
		person.setEmail("example@example.com");
		person.getCustomData().put("foo", "bar");
		EventData eventData = new EventData();
		FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), eventData, person, new Device(), new AppRelease());

		// 0
		assertTrue(criteria.isMet(fieldManager));

		// 1
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertFalse(criteria.isMet(fieldManager));

		// 2
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertTrue(criteria.isMet(fieldManager));

		// 3
		person.getCustomData().put("foo", "bar");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertTrue(criteria.isMet(fieldManager));

		// 4
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertFalse(criteria.isMet(fieldManager));

		// 5
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertTrue(criteria.isMet(fieldManager));
	}

	@Test
	public void queriesAgainstDevice() throws JSONException {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "testQueriesAgainstDevice.json");
		json = json.replace("\"OS_API_LEVEL\"", String.valueOf(Build.VERSION.SDK_INT));
		InteractionCriteria criteria = new InteractionCriteria(json);

		Device device = DeviceManager.generateNewDevice(targetContext);
		device.getCustomData().put("foo", "bar");

		EventData eventData = new EventData();
		FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), eventData, new Person(), device, new AppRelease());

		// 0
		assertTrue(criteria.isMet(fieldManager));

		// 1
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertTrue(criteria.isMet(fieldManager));

		// 2
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertFalse(criteria.isMet(fieldManager));

		// 3
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertTrue(criteria.isMet(fieldManager));

		// 4
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertTrue(criteria.isMet(fieldManager));
	}

	@Test
	public void queriesAgainstSdk() throws JSONException {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "testQueriesAgainstSdk.json");
		InteractionCriteria criteria = new InteractionCriteria(json);

		Device device = new Device();
		device.getCustomData().put("foo", "bar");
		EventData eventData = new EventData();
		FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), eventData, new Person(), device, new AppRelease());

		// 0
		assertTrue(criteria.isMet(fieldManager));

		// 1
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertTrue(criteria.isMet(fieldManager));

		// 2
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertFalse(criteria.isMet(fieldManager));
	}
}
