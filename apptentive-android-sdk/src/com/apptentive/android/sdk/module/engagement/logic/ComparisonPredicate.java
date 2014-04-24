/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;

import android.content.Context;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.storage.VersionHistoryStore;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class ComparisonPredicate extends Predicate {

	private static final String KEY_TIME_SINCE_INSTALL = "time_since_install";
	private static final String KEY_IS_UPDATE = "is_update";

	protected String query;
	protected List<Condition> conditions;

	public ComparisonPredicate(String query, Object condition) throws JSONException {
		this.query = query;
		conditions = new ArrayList<Condition>();
		if (condition instanceof JSONObject) {
			JSONObject conditionJson = (JSONObject) condition;
			// This is an object. It may contain multiple comparison operations, so unroll it and add them all in.
			@SuppressWarnings("unchecked")
			Iterator<String> it = (Iterator<String>) conditionJson.keys();
			while (it.hasNext()) {
				String conditionKey = it.next();
				Operation operation = Operation.valueOf(conditionKey);
				conditions.add(new Condition(operation, conditionJson.get(conditionKey)));
			}
		} else {
			// If it's a literal, then it has to be an EQUALITY operation. The others are always wrapped in a JSONObject.
			conditions.add(new Condition(Operation.$eq, condition));
		}
	}

	/**
	 * Makes sure that if the first parameter is a Double, the second is converted to a Double. If the first parameter is
	 * not a Double, or the second can't be converted to a Double, then simply return the second parameter.
	 *
	 * @return The second parameter, converted to a Double if it is a Number, and the first parameter is a Double. Else
	 * return the second parameter straight away.
	 */
	private Object normalize (Object one, Object two) {
		if (one instanceof Double && two instanceof Number) {
			return ((Number) two).doubleValue();
		}
		return two;
	}

	public Comparable getValue(Context context, String query) {
		QueryType queryType = QueryType.parse(query);

		switch (queryType) {
			case application_version:
				return Util.getAppVersionName(context);
			case application_build:
				return (double) Util.getAppVersionCode(context);
			default:
				if (query.startsWith(KEY_TIME_SINCE_INSTALL)) {
					ValueSubFilterType subFilterType = ValueSubFilterType.parse(query.replace(KEY_TIME_SINCE_INSTALL + "/", ""));
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
							Log.w("Unsupported sub filter type \"%s\" for query \"%s\"", subFilterType.name(), query);
					}
					return seconds;
				} else if (query.startsWith(KEY_IS_UPDATE)) {
					ValueSubFilterType subFilterType = ValueSubFilterType.parse(query.replace(KEY_IS_UPDATE + "/", ""));
					Boolean isUpdate = false;
					switch (subFilterType) {
						case version:
							isUpdate = VersionHistoryStore.isUpdate(context, VersionHistoryStore.Selector.version);
							break;
						case build:
							isUpdate = VersionHistoryStore.isUpdate(context, VersionHistoryStore.Selector.build);
							break;
						default:
							Log.w("Unsupported sub filter type \"%s\" for query \"%s\"", subFilterType.name(), query);
					}
					return isUpdate;
				} else {
					// Must be a code point / interaction
					String[] parts = query.split("/");
					boolean interaction = parts[0].equals(CodePointStore.KEY_INTERACTIONS);
					String name = parts[1];
					ValueFilterType valueFilterType = ValueFilterType.parse(parts[2]);
					ValueSubFilterType valueSubFilterType = ValueSubFilterType.parse(parts[3]);

					switch (valueFilterType) {
						case invokes:
							switch (valueSubFilterType) {
								case total: // Get total for all versions of the app.
									return (double) CodePointStore.getTotalInvokes(context, interaction, name);
								case version:
									String appVersion = Util.getAppVersionName(context);
									return (double) CodePointStore.getVersionInvokes(context, interaction, name, appVersion);
								case build:
									String appBuild = String.valueOf(Util.getAppVersionCode(context));
									return (double) CodePointStore.getBuildInvokes(context, interaction, name, appBuild);
								case time_ago:
									double timeAgo = Util.currentTimeSeconds() - CodePointStore.getLastInvoke(context, interaction, name);
									return timeAgo;
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
		return null;
	}

	@Override
	public boolean apply(Context context) {
		Comparable value = getValue(context, query);
		Log.v("Comparison Predicate: %s = %s", query, value);
		for (Condition condition : conditions) {
			condition.operand = normalize(value, condition.operand);
			Log.v("-- Compare: %s %s %s", getLoggableValue(value), condition.operation, getLoggableValue(condition.operand));
			switch (condition.operation) {
				case $gt: {
					if (value == null) {
						return false;
					}
					if (condition.operand instanceof Comparable) {
						Comparable operand = (Comparable) condition.operand;
						if (!(value.compareTo(operand) > 0)) {
							return false;
						}
					} else {
						throw new IllegalArgumentException(String.format("Can't compare %s > %s", value, condition.operand));
					}
					break;
				}
				case $gte: {
					if (value == null) {
						return false;
					}
					if (condition.operand instanceof Comparable) {
						Comparable operand = (Comparable) condition.operand;
						if (!(value.compareTo(operand) >= 0)) {
							return false;
						}
					} else {
						throw new IllegalArgumentException(String.format("Can't compare %s >= %s", value, condition.operand));
					}
					break;
				}
				case $eq: {
					if (value == null) {
						return false;
					}
					if (condition.operand instanceof Comparable) {
						Comparable operand = (Comparable) condition.operand;
						if (!value.equals(operand)) {
							return false;
						}
					} else {
						throw new IllegalArgumentException(String.format("Can't compare %s == %s", value, condition.operand));
					}
					break;
				}
				case $ne: {
					if (value == null) {
						return false;
					}
					if (condition.operand instanceof Comparable) {
						Comparable operand = (Comparable) condition.operand;
						if (value.equals(operand)) {
							return false;
						}
					} else {
						throw new IllegalArgumentException(String.format("Can't compare %s != %s", value, condition.operand));
					}
					break;
				}
				case $lte: {
					if (value == null) {
						return false;
					}
					if (condition.operand instanceof Comparable) {
						Comparable operand = (Comparable) condition.operand;
						if (!(value.compareTo(operand) <= 0)) {
							return false;
						}
					} else {
						throw new IllegalArgumentException(String.format("Can't compare %s <= %s", value, condition.operand));
					}
					break;
				}
				case $lt: {
					if (value == null) {
						return false;
					}
					if (condition.operand instanceof Comparable) {
						Comparable operand = (Comparable) condition.operand;
						if (!(value.compareTo(operand) < 0)) {
							return false;
						}
					} else {
						throw new IllegalArgumentException(String.format("Can't compare %s < %s", value, condition.operand));
					}
					break;
				}
				case $exists: {
					if (!(condition.operand instanceof Boolean)) {
						throw new IllegalArgumentException(String.format("Argument %s is not a boolean", condition.operand));
					}
					boolean shouldExist = (Boolean) condition.operand;
					boolean exists = value != null;
					return exists == shouldExist;
				}
				case $contains: {
					if (value == null) {
						return false;
					}
					boolean ret = false;
					if (value instanceof String && condition.operand instanceof String) {
						ret = ((String) value).contains((String) condition.operand);
					}
					return ret;
				}
				default:
					break;
			}
		}
		return true;
	}


	private String getLoggableValue(Object input) {
		if (input != null) {
			if (input instanceof String) {
				return "\"" + input + "\"";
			} else {
				return input.toString();
			}
		} else {
			return "null";
		}
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
