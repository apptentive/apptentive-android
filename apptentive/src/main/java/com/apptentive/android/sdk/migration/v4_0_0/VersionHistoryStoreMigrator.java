/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.migration.v4_0_0;

import android.content.SharedPreferences;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.Constants;

public class VersionHistoryStoreMigrator {

	private static final String OLD_ENTRY_SEP = "__";
	private static final String OLD_FIELD_SEP = "--";
	private static final int OLD_POSITION_TIMESTAMP = 0;
	private static final int OLD_POSITION_VERSION_CODE = 1;
	private static final int OLD_POSITION_VERSION_NAME = 2;

	private static boolean migrated_to_v2;

	public static void migrateV1ToV2(String oldFormat) {
		ApptentiveLog.i("Migrating VersionHistoryStore V1 to V2.");
		ApptentiveLog.i("V1: %s", oldFormat);
		try {
			String[] entriesOld = oldFormat.split(OLD_ENTRY_SEP);
			for (String entryOld : entriesOld) {
				String[] entryPartsOld = entryOld.split(OLD_FIELD_SEP);
				try {
					VersionHistoryStore.updateVersionHistory(
						Integer.parseInt(entryPartsOld[OLD_POSITION_VERSION_CODE]),
						entryPartsOld[OLD_POSITION_VERSION_NAME],
						Double.parseDouble(entryPartsOld[OLD_POSITION_TIMESTAMP])
					);
				} catch (Exception e) {
					ApptentiveLog.w("Error migrating old version history entry: %s", entryOld);
				}
			}
			ApptentiveLog.i("V2: %s", VersionHistoryStore.getBaseArray().toString());
		} catch (Exception e) {
			ApptentiveLog.w("Error migrating old version history entries: %s", oldFormat);
		}
	}

	static void performMigrationIfNeeded() {
		performMigrationIfNeededV1ToV2();
	}

	private static void performMigrationIfNeededV1ToV2() {
		if (migrated_to_v2) {
			return;
		}
		if (ApptentiveInternal.getInstance() != null) {
			SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
			if (prefs != null) {
				migrated_to_v2 = prefs.getBoolean(Constants.PREF_KEY_VERSION_HISTORY_V2_MIGRATED, false);
				if (migrated_to_v2) {
					return;
				}
				String versionHistoryStoreOldString = prefs.getString(Constants.PREF_KEY_VERSION_HISTORY, null);
				VersionHistoryStoreMigrator.migrateV1ToV2(versionHistoryStoreOldString);
				prefs.edit().putBoolean(Constants.PREF_KEY_VERSION_HISTORY_V2_MIGRATED, true).apply();
			}
		}
	}
}
