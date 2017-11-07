/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement.criteria;

import android.support.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionCriteria;
import com.apptentive.android.sdk.module.engagement.logic.FieldManager;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.AppReleaseManager;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.EventData;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.VersionHistory;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CodePointAndInteractionStoreTest extends ApptentiveTestCaseBase {

	private static final String TEST_DIR = "engagement/criteria/";

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	@Test
	public void codePointInvokesTotal() {
		String json = loadTextAssetAsString(TEST_DIR + "testCodePointInvokesTotal.json");
		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			EventData eventData = new EventData();
			FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), eventData, new Person(), new Device(), new AppRelease());

			// 0 - $gt
			ApptentiveLog.e("Test $gt");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));

			// 1 - $gte
			eventData.clear();
			ApptentiveLog.e("Test $gte");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));

			// 2 - $ne
			eventData.clear();
			ApptentiveLog.e("Test $ne");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));

			// 3 - $eq
			eventData.clear();
			ApptentiveLog.e("Test $eq");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));

			// 4 - :
			eventData.clear();
			ApptentiveLog.e("Test :");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));

			// 5 - $lte
			eventData.clear();
			ApptentiveLog.e("Test $lte");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));

			// 6 - $lt
			eventData.clear();
			ApptentiveLog.e("Test $lt");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
		} catch (JSONException e) {
			ApptentiveLog.e(e, "Error parsing test JSON.");
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	@Test
	public void codePointInvokesVersionCode() {
		String json = loadTextAssetAsString(TEST_DIR + "testCodePointInvokesVersionCode.json");
		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			EventData eventData = new EventData();
			FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), eventData, new Person(), new Device(), new AppRelease());

			// 0 - $gt
			eventData.clear();
			ApptentiveLog.e("Test $gt");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));

			// 1 - $gte
			eventData.clear();
			ApptentiveLog.e("Test $gte");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));

			// 2 - $ne
			eventData.clear();
			ApptentiveLog.e("Test $ne");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));

			// 3 - $eq
			eventData.clear();
			ApptentiveLog.e("Test $eq");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));

			// 4 - :
			eventData.clear();
			ApptentiveLog.e("Test :");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));

			// 5 - $lte
			eventData.clear();
			ApptentiveLog.e("Test $lte");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));

			// 6 - $lt
			eventData.clear();
			ApptentiveLog.e("Test $lt");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));

		} catch (JSONException e) {
			ApptentiveLog.e(e, "Error parsing test JSON.");
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	@Test
	public void codePointInvokesVersionName() {
		String json = loadTextAssetAsString(TEST_DIR + "testCodePointInvokesVersionName.json");
		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			EventData eventData = new EventData();
			FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), eventData, new Person(), new Device(), new AppRelease());

			// 0 - $gt
			eventData.clear();
			ApptentiveLog.e("Test $gt");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));

			// 1 - $gte
			eventData.clear();
			ApptentiveLog.e("Test $gte");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));

			// 2 - $ne
			eventData.clear();
			ApptentiveLog.e("Test $ne");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));

			// 3 - $eq
			eventData.clear();
			ApptentiveLog.e("Test $eq");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));

			// 4 - :
			eventData.clear();
			ApptentiveLog.e("Test :");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));

			// 5 - $lte
			eventData.clear();
			ApptentiveLog.e("Test $lte");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));

			// 6 - $lt
			eventData.clear();
			ApptentiveLog.e("Test $lt");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertFalse(criteria.isMet(fieldManager));

		} catch (JSONException e) {
			ApptentiveLog.e(e, "Error parsing test JSON.");
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	@Test
	public void codePointLastInvokedAt() {
		String json = loadTextAssetAsString(TEST_DIR + "testCodePointLastInvokedAt.json");
		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			EventData eventData = new EventData();
			FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), eventData, new Person(), new Device(), new AppRelease());

			// 0 - $after
			eventData.clear();
			ApptentiveLog.e("Test $after");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			assertTrue(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			sleep(300);
			assertTrue(criteria.isMet(fieldManager));
			sleep(300);
			assertFalse(criteria.isMet(fieldManager));

			// 1 - $ne
			eventData.clear();
			ApptentiveLog.e("Test $ne");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			sleep(300);
			assertTrue(criteria.isMet(fieldManager));
			sleep(300);
			assertTrue(criteria.isMet(fieldManager));

			// 2 - $eq // There's no easy way to test this unless we contrive the times.
			eventData.clear();
			ApptentiveLog.e("Test $eq");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			sleep(300);
			assertFalse(criteria.isMet(fieldManager));
			sleep(300);
			assertFalse(criteria.isMet(fieldManager));

			// 3 - : // Ditto
			eventData.clear();
			ApptentiveLog.e("Test :");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			sleep(300);
			assertFalse(criteria.isMet(fieldManager));
			sleep(300);
			assertFalse(criteria.isMet(fieldManager));

			// 4 - $before
			eventData.clear();
			ApptentiveLog.e("Test $before");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
			assertFalse(criteria.isMet(fieldManager));
			eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.code.point");
			sleep(300);
			assertFalse(criteria.isMet(fieldManager));
			sleep(300);
			assertTrue(criteria.isMet(fieldManager));

		} catch (JSONException e) {
			ApptentiveLog.e(e, "Error parsing test JSON.");
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	@Test
	public void interactionInvokesTotal() throws JSONException {
		AppRelease appRelease = AppReleaseManager.generateCurrentAppRelease(targetContext, null);
		String json = loadTextAssetAsString(TEST_DIR + "testInteractionInvokesTotal.json");

		InteractionCriteria criteria = new InteractionCriteria(json);

		EventData eventData = new EventData();
		FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), eventData, new Person(), new Device(), appRelease);

		// 0 - $gt
		eventData.clear();
		ApptentiveLog.e("Test $gt");
		assertFalse(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertFalse(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertFalse(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertTrue(criteria.isMet(fieldManager));

		// 1 - $gte
		eventData.clear();
		ApptentiveLog.e("Test $gte");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertFalse(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertFalse(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertTrue(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertTrue(criteria.isMet(fieldManager));

		// 2 - $ne
		eventData.clear();
		ApptentiveLog.e("Test $ne");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertTrue(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertTrue(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertFalse(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertTrue(criteria.isMet(fieldManager));

		// 3 - $eq
		eventData.clear();
		ApptentiveLog.e("Test $eq");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertFalse(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertFalse(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertTrue(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertFalse(criteria.isMet(fieldManager));

		// 4 - :
		eventData.clear();
		ApptentiveLog.e("Test :");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertFalse(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertFalse(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertTrue(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertFalse(criteria.isMet(fieldManager));

		// 5 - $lte
		eventData.clear();
		ApptentiveLog.e("Test $lte");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertTrue(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertTrue(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertTrue(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertFalse(criteria.isMet(fieldManager));

		// 6 - $lt
		eventData.clear();
		ApptentiveLog.e("Test $lt");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch.code.point");
		assertTrue(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertTrue(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertFalse(criteria.isMet(fieldManager));
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "test.interaction");
		assertFalse(criteria.isMet(fieldManager));

		ApptentiveLog.e("Finished test.");
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}
}
