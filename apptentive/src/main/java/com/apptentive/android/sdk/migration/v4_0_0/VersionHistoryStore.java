/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.migration.v4_0_0;

import android.content.SharedPreferences;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.RuntimeUtils;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

/**
 * Stores version history in JSON, in SharedPreferences.
 */
public class VersionHistoryStore {

	private static List<VersionHistoryEntry> versionHistoryEntries;

	static {
		VersionHistoryStoreMigrator.performMigrationIfNeeded();
	}

	private VersionHistoryStore() {
	}

	private static void save() {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
		JSONArray baseArray = getBaseArray();
		if (baseArray != null) {
			prefs.edit().putString(Constants.PREF_KEY_VERSION_HISTORY_V2, baseArray.toString()).apply();
		}
	}

	private static void ensureLoaded() {
		if (versionHistoryEntries == null) {
			versionHistoryEntries = new ArrayList<VersionHistoryEntry>();
			SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
			if (prefs != null) {
				try {
					String json = prefs.getString(Constants.PREF_KEY_VERSION_HISTORY_V2, "[]");
					JSONArray baseArray = new JSONArray(json);
					for (int i = 0; i < baseArray.length(); i++) {
						VersionHistoryEntry entry = new VersionHistoryEntry(baseArray.getJSONObject(i));
						versionHistoryEntries.add(entry);
					}
				} catch (Exception e) {
					ApptentiveLog.w(CONVERSATION, e, "Error loading VersionHistoryStore.");
					logException(e);
				}
			}
		}
	}

	public static synchronized void clear() {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
		prefs.edit().remove(Constants.PREF_KEY_VERSION_HISTORY_V2).apply();
		versionHistoryEntries.clear();
	}

	public static synchronized void updateVersionHistory(int newVersionCode, String newVersionName) {
		updateVersionHistory(newVersionCode, newVersionName, Util.currentTimeSeconds());
	}

	public static synchronized void updateVersionHistory(Integer newVersionCode, String newVersionName, double date) {
		ensureLoaded();
		try {
			boolean exists = false;
			for (VersionHistoryEntry entry : versionHistoryEntries) {
				if (entry.getVersionCode() == newVersionCode && entry.getVersionName().equals(newVersionName)) {
					exists = true;
				}
			}
			// Only modify the store if the version hasn't been seen.
			if (!exists) {
				VersionHistoryEntry entry = new VersionHistoryEntry(newVersionCode, newVersionName, date);
				ApptentiveLog.v(CONVERSATION, "Adding Version History entry: %s", entry);
				versionHistoryEntries.add(new VersionHistoryEntry(newVersionCode, newVersionName, date));
				save();
			}
		} catch (Exception e) {
			ApptentiveLog.w(CONVERSATION, e, "Error updating VersionHistoryStore.");
			logException(e);
		}
	}

	/**
	 * Returns the number of seconds since the first time we saw this release of the app. Since the version entries are
	 * always stored in order, the first matching entry happened first.
	 *
	 * @param selector - The type of version entry we are looking for: total, version, or build.
	 * @return A DateTime representing the number of seconds since we first saw the desired app release entry. A DateTime with current time if never seen.
	 */
	public static synchronized Apptentive.DateTime getTimeAtInstall(Selector selector) {
		ensureLoaded();
		for (VersionHistoryEntry entry : versionHistoryEntries) {
			switch (selector) {
				case total:
					// Since the list is ordered, this will be the first and oldest entry.
					return new Apptentive.DateTime(entry.getTimestamp());
				case version_code:
					if (entry.getVersionCode() == RuntimeUtils.getAppVersionCode(ApptentiveInternal.getInstance().getApplicationContext())) {
						return new Apptentive.DateTime(entry.getTimestamp());
					}
					break;
				case version_name:
					Apptentive.Version entryVersionName = new Apptentive.Version();
					Apptentive.Version currentVersionName = new Apptentive.Version();
					entryVersionName.setVersion(entry.getVersionName());
					currentVersionName.setVersion(RuntimeUtils.getAppVersionName(ApptentiveInternal.getInstance().getApplicationContext()));
					if (entryVersionName.equals(currentVersionName)) {
						return new Apptentive.DateTime(entry.getTimestamp());
					}
					break;
			}
		}
		return new Apptentive.DateTime(Util.currentTimeSeconds());
	}

	/**
	 * Returns true if the current version or build is not the first version or build that we have seen. Basically, it just
	 * looks for two or more versions or builds.
	 *
	 * @param selector - The type of version entry we are looking for: version, or build.
	 * @return True if there are records with more than one version or build, depending on the value of selector.
	 */
	public static synchronized boolean isUpdate(Selector selector) {
		ensureLoaded();
		Set<String> uniques = new HashSet<String>();
		for (VersionHistoryEntry entry : versionHistoryEntries) {
			switch (selector) {
				case version_name:
					uniques.add(entry.getVersionName());
					break;
				case version_code:
					uniques.add(String.valueOf(entry.getVersionCode()));
					break;
				default:
					break;
			}
		}
		return uniques.size() > 1;
	}

	public static synchronized VersionHistoryEntry getLastVersionSeen() {
		ensureLoaded();
		if (versionHistoryEntries != null && !versionHistoryEntries.isEmpty()) {
			return versionHistoryEntries.get(versionHistoryEntries.size() - 1);
		}
		return null;
	}

	/**
	 * Don't use this directly. Used for debugging only.
	 *
	 * @return the base JSONArray
	 */
	public static JSONArray getBaseArray() {
		ensureLoaded();
		JSONArray baseArray = new JSONArray();
		for (VersionHistoryEntry entry : versionHistoryEntries) {
			baseArray.put(entry);
		}
		return baseArray;
	}

	public enum Selector {
		total,
		version_code,
		version_name,
		other;

		public static Selector parse(String name) {
			try {
				return Selector.valueOf(name);
			} catch (IllegalArgumentException e) {
				// Ignore
			}
			return other;
		}
	}
}
