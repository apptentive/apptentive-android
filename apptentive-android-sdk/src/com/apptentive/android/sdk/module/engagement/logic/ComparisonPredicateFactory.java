package com.apptentive.android.sdk.module.engagement.logic;

import android.content.Context;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.storage.VersionHistoryStore;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class ComparisonPredicateFactory {

	private static final String KEY_TIME_SINCE_INSTALL = "time_since_install";
	private static final String KEY_IS_UPDATE = "is_update";

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
			default:
				if (key.startsWith(KEY_TIME_SINCE_INSTALL)) {
					ValueSubFilterType subFilterType = ValueSubFilterType.parse(key.replace(KEY_TIME_SINCE_INSTALL + "/", ""));
					Double seconds = null;
					switch (subFilterType) {
						case total:
							seconds = VersionHistoryStore.getTimeSinceVersionFirstSeen(context, VersionHistoryStore.Selector.total);
							break;
						case version:
							seconds = VersionHistoryStore.getTimeSinceVersionFirstSeen(context, VersionHistoryStore.Selector.version);
							break;
						case build:
							seconds = VersionHistoryStore.getTimeSinceVersionFirstSeen(context, VersionHistoryStore.Selector.build);
							break;
						default:
							Log.w("Unsupported sub filter type \"%s\" for key \"%s\"", subFilterType.name(), key);
					}
					return new NumberComparisonPredicate(key, seconds, condition);
				} else if (key.startsWith(KEY_IS_UPDATE)) {
					ValueSubFilterType subFilterType = ValueSubFilterType.parse(key.replace(KEY_IS_UPDATE + "/", ""));
					Boolean isUpdate = false;
					switch (subFilterType) {
						case version:
							isUpdate = VersionHistoryStore.isUpdate(context, VersionHistoryStore.Selector.version);
							break;
						case build:
							isUpdate = VersionHistoryStore.isUpdate(context, VersionHistoryStore.Selector.build);
							break;
						default:
							Log.w("Unsupported sub filter type \"%s\" for key \"%s\"", subFilterType.name(), key);
					}
					return new BooleanComparisonPredicate(key, isUpdate, condition);
				} else {
					// Must be a code point / interaction
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
									double timeAgo = Util.currentTimeSeconds() - CodePointStore.getLastInvoke(context, interaction, name);
									return new NumberComparisonPredicate(key, timeAgo, condition);
								default:
									break;
							}
							break;
						default:
							break;
					}
				}
				break;
		}
		Log.w("Unable to parse predicate: %s => %s", key, condition.toString());
		return null;
	}

	private enum QueryType {
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
