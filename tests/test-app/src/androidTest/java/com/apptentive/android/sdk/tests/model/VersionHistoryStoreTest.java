/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.model;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.storage.VersionHistoryEntry;
import com.apptentive.android.sdk.storage.VersionHistoryStore;
import com.apptentive.android.sdk.storage.VersionHistoryStoreMigrator;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.tests.util.FileUtil;
import com.apptentive.android.sdk.util.JsonDiffer;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * There will always be a VersionHistoryEntry present due to the SDK initialializing, so call resetDevice() if you don't want that in your test.
 */
public class VersionHistoryStoreTest extends ApptentiveInstrumentationTestCase {

	public void testVersionHistoryStoreMigration() {
		resetDevice();
		String oldFormat = FileUtil.loadTextAssetAsString(getTestContext(), "model/versionHistoryStoreOld.txt");
		VersionHistoryStoreMigrator.migrateV1ToV2(oldFormat);
		try {
			JSONArray expected = new JSONArray(FileUtil.loadTextAssetAsString(getTestContext(), "model/versionHistoryStore.json"));
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

	public void testVersionHistoryStoreOrdering() {
		try {
			JSONArray expected = new JSONArray(FileUtil.loadTextAssetAsString(getTestContext(), "model/versionHistoryStore.json"));
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

	public void testVersionHistoryStoreGetLatestVersion() {
		VersionHistoryStore.updateVersionHistory(1, "3.3.0", 1.472853954087E9d);
		VersionHistoryStore.updateVersionHistory(2, "3.3.1", 1.472854098019E9d);

		VersionHistoryEntry latestEntry = VersionHistoryStore.getLastVersionSeen();
		assertEquals(1.472854098019E9d, latestEntry.getTimestamp());
		assertEquals(2, latestEntry.getVersionCode());
		assertEquals("3.3.1", latestEntry.getVersionName());
	}

	public void testVersionHistoryStoreTimeAtInstall() {
		resetDevice();
		VersionHistoryStore.updateVersionHistory(1, "1.0.0", 1.472853951000E9d);
		VersionHistoryStore.updateVersionHistory(2, "1.0.1", 1.472853952000E9d);
		VersionHistoryStore.updateVersionHistory(3, "1.1.0", 1.472853953000E9d);
		VersionHistoryStore.updateVersionHistory(4, "1.2.0", 1.472853954000E9d);
		VersionHistoryStore.updateVersionHistory(5, "2.0.0", 1.472853955000E9d);
		VersionHistoryStore.updateVersionHistory(6, "2.1.0", 1.472853956000E9d);

		// Use the JSONDiffer here, because it will do a proper "float equality" check.
		assertTrue(JsonDiffer.areObjectsEqual(new Apptentive.DateTime(1.472853950001E9d), VersionHistoryStore.getTimeAtInstall(VersionHistoryStore.Selector.total)));
		assertTrue(JsonDiffer.areObjectsEqual(new Apptentive.DateTime(1.472853950004E9d), VersionHistoryStore.getTimeAtInstall(VersionHistoryStore.Selector.version_code)));
		assertTrue(JsonDiffer.areObjectsEqual(new Apptentive.DateTime(1.472853950005E9d), VersionHistoryStore.getTimeAtInstall(VersionHistoryStore.Selector.version_name)));
	}

	public void testVersionHistoryStoreIsUpdate() {
		resetDevice();
		assertFalse(VersionHistoryStore.isUpdate(VersionHistoryStore.Selector.version_code));
		assertFalse(VersionHistoryStore.isUpdate(VersionHistoryStore.Selector.version_name));
		VersionHistoryStore.updateVersionHistory(1, "1.0.0", 1.472853951000E9d);
		assertFalse(VersionHistoryStore.isUpdate(VersionHistoryStore.Selector.version_code));
		assertFalse(VersionHistoryStore.isUpdate(VersionHistoryStore.Selector.version_name));
		VersionHistoryStore.updateVersionHistory(1, "1.0.1", 1.472853952000E9d);
		assertFalse(VersionHistoryStore.isUpdate(VersionHistoryStore.Selector.version_code));
		assertTrue(VersionHistoryStore.isUpdate(VersionHistoryStore.Selector.version_name));
		VersionHistoryStore.updateVersionHistory(2, "1.0.1", 1.472853953000E9d);
		assertTrue(VersionHistoryStore.isUpdate(VersionHistoryStore.Selector.version_code));
		assertTrue(VersionHistoryStore.isUpdate(VersionHistoryStore.Selector.version_name));
	}
}
