package com.apptentive.android.sdk.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class VersionHistoryStore {
	public static final String FIELD_SEP = "--";
	public static final String ENTRY_SEP = "__";


	/**
	 * Two simple lists of version number and update time. Version number is base64 encoded and followed by a ':" and the
	 * timestamp. Each entry ends with a ';' to separate it from subsequent entries. A list is stored for both the
	 * version name and version code.
	 */
	public static void updateVersionHistory(Context context, Integer newVersionCode, String newVersionName) {
		updateVersionHistory(context, newVersionCode, newVersionName, System.currentTimeMillis());
	}

	public static void updateVersionHistory(Context context, Integer newVersionCode, String newVersionName, long date) {
		Log.d("Updating version info: %d, %s @%d", newVersionCode, newVersionName, date);
		Double now = (double) date / 1000;
		List<VersionHistoryEntry> versionHistory = getVersionHistory(context);
		for (VersionHistoryEntry entry : versionHistory) {
			if (newVersionCode.equals(entry.versionCode)) {
				return; // Already recorded.
			}
		}
		versionHistory.add(new VersionHistoryEntry(now, newVersionCode, newVersionName));
		saveVersionHistory(context, versionHistory);
	}

	public static VersionHistoryEntry getLastVersionSeen(Context context) {
		List<VersionHistoryEntry> entries = getVersionHistory(context);
		if (entries != null && !entries.isEmpty()) {
			return entries.get(entries.size() - 1);
		}
		return null;
	}

	/**
	 * Returns a map containing two maps of string to string. The first map is named "versionName" and the second is named
	 * "versionCode". Each of those maps from a Double timestamp to a String representing the version code of version name.
	 *
	 * @return A List of VersionHistoryEntry objects.
	 */
	public static List<VersionHistoryEntry> getVersionHistory(Context context) {
		List<VersionHistoryEntry> versionHistory = new ArrayList<VersionHistoryEntry>();
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String versionHistoryString = prefs.getString(Constants.PREF_KEY_VERSION_HISTORY, null);
		if (versionHistoryString != null) {
			String[] parts = versionHistoryString.split(ENTRY_SEP);
			for (int i = 0; i < parts.length; i++) {
				versionHistory.add(new VersionHistoryEntry(parts[i]));
			}
		}
		return versionHistory;
	}

	private static void saveVersionHistory(Context context, List<VersionHistoryEntry> versionHistory) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		StringBuilder versionHistoryString = new StringBuilder();
		for (VersionHistoryEntry entry : versionHistory) {
			versionHistoryString.append(entry.toString()).append(ENTRY_SEP);
		}
		prefs.edit().putString(Constants.PREF_KEY_VERSION_HISTORY, versionHistoryString.toString()).commit();
	}

	public static class VersionHistoryEntry {
		public Double seconds;
		public Integer versionCode;
		public String versionName;

		public VersionHistoryEntry(String encoded) {
			if (encoded != null) {
				// Remove entry separator and split on field separator.
				String[] parts = encoded.replace(ENTRY_SEP, "").split(FIELD_SEP);
				seconds = Double.valueOf(parts[0]);
				versionCode = Integer.parseInt(parts[1]);
				versionName = parts[2];
			}
		}

		public VersionHistoryEntry(Double seconds, Integer versionCode, String versionName) {
			this.seconds = seconds;
			this.versionCode = versionCode;
			this.versionName = versionName;
		}

		@Override
		public String toString() {
			return String.valueOf(seconds) + FIELD_SEP + String.valueOf(versionCode) + FIELD_SEP + versionName;
		}
	}
}
