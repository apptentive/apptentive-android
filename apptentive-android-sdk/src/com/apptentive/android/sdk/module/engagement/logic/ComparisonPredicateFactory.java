package com.apptentive.android.sdk.module.engagement.logic;

import android.content.Context;
import android.text.format.DateUtils;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.storage.VersionHistoryStore;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public class ComparisonPredicateFactory {

	/**
	 * Resolves the key to a value, and then returns the appropriate Predicate type.
	 */
	public static ComparisonPredicate generatePredicate(Context context, String key, Object condition) throws JSONException {
		ComparisonPredicate ret;

		QueryType queryType = QueryType.parse(key);

		switch (queryType) {
			case application_version:
				return new StringComparisonPredicate(key, Util.getAppVersionName(context), condition);
			case application_build:
				return new NumberComparisonPredicate(key, (double) Util.getAppVersionCode(context), condition);
			case days_since_install: {
				long value = 0l;
				VersionHistoryStore.VersionHistoryEntry latestEntry = VersionHistoryStore.getLastVersionSeen(context);
				if (latestEntry != null) {
					Double seconds = latestEntry.seconds;
					long elapsed = System.currentTimeMillis() - (long) (seconds * 1000);
					value = elapsed / DateUtils.DAY_IN_MILLIS; // Integer division on purpose
				}
				return new NumberComparisonPredicate(key, (double) value, condition);
			}
			case days_since_upgrade:
				long value = 0l;
				VersionHistoryStore.VersionHistoryEntry latestEntry = VersionHistoryStore.getLastVersionSeen(context);
				if (latestEntry != null) {
					Double seconds = latestEntry.seconds;
					long elapsed = System.currentTimeMillis() - (long) (seconds * 1000);
					value = elapsed / DateUtils.DAY_IN_MILLIS; // Integer division on purpose
				}
				return new NumberComparisonPredicate(key, (double) value, condition);
			default: // Must be a code point / interaction
				String[] parts = key.split("/");
				boolean interaction = parts[0].equals(CodePointStore.KEY_INTERACTIONS);
				String name = parts[1];
				ValueFilterType valueFilterType = ValueFilterType.parse(parts[2]);
				ValueSubFilterType valueSubFilterType = ValueSubFilterType.parse(parts[3]);

				switch (valueFilterType) {
					case invokes:
						switch (valueSubFilterType) {
							case total: // Get total for all versions of the app.
								return new NumberComparisonPredicate(key, (double) CodePointStore.getTotalInvokes(context, interaction, name), condition);
							case version:
								String appVersion = Util.getAppVersionName(context);
								return new NumberComparisonPredicate(key, (double) CodePointStore.getVersionInvokes(context, interaction, name, appVersion), condition);
							case build:
								String appBuild = String.valueOf(Util.getAppVersionCode(context));
								return new NumberComparisonPredicate(key, (double) CodePointStore.getBuildInvokes(context, interaction, name, appBuild), condition);
							case time_ago:
								double timeAgo = Util.getCurrentTime() - CodePointStore.getLastInvoke(context, interaction, name);
								return new NumberComparisonPredicate(key, timeAgo, condition);
							default:
								break;
						}
						break;
					default:
						break;
				}
				break;
		}
		Log.w("Unable to parse predicate: %s => %s", key, condition.toString());
		return null;
	}

	private enum QueryType {
		days_since_install,
		days_since_upgrade,
		application_version,
		application_build,
		other;

		public static QueryType parse(String name) {
			try {
				return QueryType.valueOf(name);
			} catch (IllegalArgumentException e) {
			}
			return other;
		}
	}

	private enum ValueFilterType {
		invokes,
		other;

		public static ValueFilterType parse(String name) {
			try {
				return ValueFilterType.valueOf(name);
			} catch (IllegalArgumentException e) {
			}
			return other;
		}
	}

	private enum ValueSubFilterType {
		total,
		version,
		build,
		time_ago,
		other;

		public static ValueSubFilterType parse(String name) {
			try {
				return ValueSubFilterType.valueOf(name);
			} catch (IllegalArgumentException e) {
			}
			return other;
		}
	}
}
