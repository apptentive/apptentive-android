/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.ApptentiveLog;

public class VersionHistoryStoreMigrator {

	private static final String OLD_ENTRY_SEP = "__";
	private static final String OLD_FIELD_SEP = "--";
	private static final int OLD_POSITION_TIMESTAMP = 0;
	private static final int OLD_POSITION_VERSION_CODE = 1;
	private static final int OLD_POSITION_VERSION_NAME = 2;


	public static void migrate(String oldFormat) {
		try {
			String[] entriesOld = oldFormat.split(OLD_ENTRY_SEP);
			for (String entryOld : entriesOld) {
				String[] entryPartsOld = entryOld.replace(OLD_ENTRY_SEP, "").split(OLD_FIELD_SEP);
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
		} catch (Exception e) {
			ApptentiveLog.w("Error migrating old version history entries: %s", oldFormat);
		}
	}
}
