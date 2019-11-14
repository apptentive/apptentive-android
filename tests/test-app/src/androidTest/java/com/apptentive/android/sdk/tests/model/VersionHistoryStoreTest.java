/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.model;

import androidx.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.migration.v4_0_0.VersionHistoryStore;
import com.apptentive.android.sdk.migration.v4_0_0.VersionHistoryStoreMigrator;
import com.apptentive.android.sdk.storage.VersionHistory;
import com.apptentive.android.sdk.storage.VersionHistoryItem;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;
import com.apptentive.android.sdk.util.JsonDiffer;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * There will always be a VersionHistoryEntry present due to the SDK initializing, so call resetDevice() if you don't want that in your test.
 */
@RunWith(AndroidJUnit4.class)
public class VersionHistoryStoreTest extends ApptentiveTestCaseBase {

	@Before
	public void setUp() {
		super.setUp();
		ApptentiveInternal.setInstance(new ApptentiveInternal(targetContext));
	}

	@Test
	public void testVersionHistoryStoreMigration() {
		String oldFormat = loadTextAssetAsString("model/versionHistoryStoreOld.txt");

		VersionHistoryStoreMigrator.migrateV1ToV2(oldFormat);
		try {
			JSONArray expected = new JSONArray(loadTextAssetAsString("model/versionHistoryStore.json"));
			JSONArray result = VersionHistoryStore.getBaseArray();
			boolean equal = JsonDiffer.areObjectsEqual(result, expected);
			if (!equal) {
				ApptentiveLog.e("Expected:\n%s", expected);
				ApptentiveLog.e("Result:\n%s", result);
			}
			assertTrue("VersionHistoryStoreMigrator migrated incorrectly.", equal);
		} catch (JSONException e) {
			assertNull(e);
			throw new RuntimeException("Error processing JSON in test.", e);
		}
	}

	// Update to latest API below here:

	@Test
	public void testVersionHistoryStoreOrdering() {
		try {
			JSONArray expected = new JSONArray(loadTextAssetAsString("model/versionHistoryStore.json"));
			VersionHistoryStore.updateVersionHistory(1, "3.3.0", 1.472853954087E9d);
			VersionHistoryStore.updateVersionHistory(2, "3.3.1", 1.472854098019E9d);
			JSONArray result = VersionHistoryStore.getBaseArray();
			boolean equal = JsonDiffer.areObjectsEqual(result, expected);
			if (!equal) {
				ApptentiveLog.e("Expected:\n%s", expected);
				ApptentiveLog.e("Result:\n%s", result);
			}
			assertTrue("VersionHistoryStore failed to maintain version history order.", equal);
		} catch (JSONException e) {
			assertNull(e);
			throw new RuntimeException("Error processing JSON in test.", e);
		}
	}

	@Test
	public void testVersionHistoryStoreGetLatestVersion() {
		VersionHistory versionHistory = new VersionHistory();
		versionHistory.updateVersionHistory(1.472853954087E9d, 1, "3.3.0");
		versionHistory.updateVersionHistory(1.472854098019E9d, 2, "3.3.1");

		VersionHistoryItem latestEntry = versionHistory.getLastVersionSeen();
		assertNotNull(latestEntry);
		assertEquals(1.472854098019E9d, latestEntry.getTimestamp(), .000001d);
		assertEquals(2, latestEntry.getVersionCode());
		assertEquals("3.3.1", latestEntry.getVersionName());
	}

	@Test
	public void testVersionHistoryStoreTimeAtInstall() {
		VersionHistory versionHistory = new VersionHistory();
		versionHistory.updateVersionHistory(1.472853951000E9d, 1, "1.0.0");
		versionHistory.updateVersionHistory(1.472853952000E9d, 2, "1.0.1");
		versionHistory.updateVersionHistory(1.472853953000E9d, 3, "1.1.0");
		versionHistory.updateVersionHistory(1.472853954000E9d, versionCode, "1.2.0");
		versionHistory.updateVersionHistory(1.472853955000E9d, 10, "2.0.0");
		versionHistory.updateVersionHistory(1.472853956000E9d, 15, "2.1.0");

		assertTrue(JsonDiffer.areObjectsEqual(new Apptentive.DateTime(1.472853951000E9d).toJSONObject(), versionHistory.getTimeAtInstallTotal().toJSONObject()));
		assertTrue(JsonDiffer.areObjectsEqual(new Apptentive.DateTime(1.472853954000E9d).toJSONObject(), versionHistory.getTimeAtInstallForVersionCode(versionCode).toJSONObject()));
		assertTrue(JsonDiffer.areObjectsEqual(new Apptentive.DateTime(1.472853955000E9d).toJSONObject(), versionHistory.getTimeAtInstallForVersionName(versionName).toJSONObject()));
	}

	@Test
	public void testVersionHistoryStoreIsUpdate() {
		VersionHistory versionHistory = new VersionHistory();

		assertFalse(versionHistory.isUpdateForVersionCode());
		assertFalse(versionHistory.isUpdateForVersionName());

		versionHistory.updateVersionHistory(1.472853951000E9d, 1, "1.0.0");
		assertFalse(versionHistory.isUpdateForVersionCode());
		assertFalse(versionHistory.isUpdateForVersionName());

		versionHistory.updateVersionHistory(1.472853952000E9d, 1, "1.0.1");
		assertFalse(versionHistory.isUpdateForVersionCode());
		assertTrue(versionHistory.isUpdateForVersionName());

		versionHistory.updateVersionHistory(1.472853953000E9d, 2, "1.0.1");
		assertTrue(versionHistory.isUpdateForVersionCode());
		assertTrue(versionHistory.isUpdateForVersionName());
	}
}
