/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.migration.v4_0_0;

import com.apptentive.android.sdk.storage.EventRecord;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>All public methods altering code point values should be synchronized.</p>
 * <p>Example:</p>
 * <pre>
 * {
 *   "code_point": {
 *     "codePoint1": {
 *       "last": 1234567890,
 *       "total": 6,
 *       "version": {
 *         "1.1": 4,
 *         "1.2": 2
 *       },
 *       "build": {
 *         "5": 4,
 *         "6": 2
 *       }
 *     }
 *   },
 *   "interactions": {
 *     "526fe2836dd8bf546a00000c": {
 *       "last": 1234567890.4,
 *       "total": 6,
 *       "version": {
 *         "1.1": 4,
 *         "1.2": 2
 *       },
 *       "build": {
 *         "5": 4,
 *         "6": 2
 *       }
 *     }
 *   }
 * }
 * </pre>
 */
public class CodePointStore {

	private JSONObject store;

	private static final String KEY_CODE_POINT = "code_point";
	private static final String KEY_INTERACTIONS = "interactions";
	private static final String KEY_LAST = "last"; // The last time this codepoint was seen.
	private static final String KEY_TOTAL = "total"; // The total times this code point was seen.
	private static final String KEY_VERSION_NAME = "version";
	private static final String KEY_VERSION_CODE = "build";

	public CodePointStore(String json) throws JSONException {
		store = new JSONObject(json);
	}

	public Map<String, EventRecord> migrateCodePoints() throws JSONException {
		return migrateRecords(false);
	}

	public Map<String, EventRecord> migrateInteractions() throws JSONException {
		return migrateRecords(true);
	}

	public Map<String, EventRecord> migrateRecords(boolean interaction) throws JSONException {
		JSONObject recordContainer = store.optJSONObject(interaction ? KEY_INTERACTIONS : KEY_CODE_POINT);
		if (recordContainer != null) {
			Map<String, EventRecord> ret = new HashMap<>();
			Iterator<String> recordNames = recordContainer.keys();
			while (recordNames.hasNext()) {
				String recordName = recordNames.next();
				JSONObject record = recordContainer.getJSONObject(recordName);
				EventRecord eventRecord = new EventRecord();
				eventRecord.setLast(record.getDouble(KEY_LAST));
				eventRecord.setTotal(record.getLong(KEY_TOTAL));

				Map<Integer, Long> versionCodes = new HashMap<>();
				JSONObject versionCodesOld = record.getJSONObject(KEY_VERSION_CODE);
				if (versionCodesOld != null) {
					Iterator<String> versionCodesIterator = versionCodesOld.keys();
					if (versionCodesIterator != null) {
						while (versionCodesIterator.hasNext()) {
							String versionCodeOld = versionCodesIterator.next();
							Long count = versionCodesOld.getLong(versionCodeOld);
							versionCodes.put(Integer.parseInt(versionCodeOld), count);
						}
					}
					eventRecord.setVersionCodes(versionCodes);
				}

				Map<String, Long> versionNames = new HashMap<>();
				JSONObject versionNamesOld = record.getJSONObject(KEY_VERSION_NAME);
				if (versionNamesOld != null) {
					Iterator<String> versionNamesIterator = versionNamesOld.keys();
					if (versionNamesIterator != null) {
						while (versionNamesIterator.hasNext()) {
							String versionNameOld = versionNamesIterator.next();
							Long count = versionNamesOld.getLong(versionNameOld);
							versionNames.put(versionNameOld, count);
						}
					}
					eventRecord.setVersionNames(versionNames);
				}
				ret.put(recordName, eventRecord);
			}
			return ret;
		}
		return null;
	}
}
