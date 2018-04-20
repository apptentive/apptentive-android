/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import static com.apptentive.android.sdk.ApptentiveLogTag.INTERACTIONS;

/**
 * @author Sky Kelsey
 */
public class ClauseParser {
	private static final String KEY_COMPLEX_TYPE = "_type";

	public static Clause parse(String json) throws JSONException {
		ApptentiveLog.v(INTERACTIONS, "+ Parsing Interaction Criteria.");
		if (json == null) {
			ApptentiveLog.e(INTERACTIONS, "+ Interaction Criteria is null.");
			return null;
		}
		JSONObject root = new JSONObject(json);
		Clause ret = ClauseParser.parse(null, root);
		ApptentiveLog.v(INTERACTIONS, "+ Finished parsing Interaction Criteria.");
		return ret;
	}

	public static Clause parse(String key, Object value) throws JSONException {
		if (key == null) {
			// The Root object, and objects inside arrays should be treated as $and.
			key = LogicalOperator.$and.name();
		}
		key = key.trim();
		LogicalOperator operator = LogicalOperator.parse(key);
		switch (operator) {
			case $or:
				return new LogicalClause(key, value);
			case $and:
				return new LogicalClause(key, value);
			case $not:
				return new LogicalClause(key, value);
			default: {
				return new ConditionalClause(key, value);
			}
		}
	}

	/**
	 * Constructs complex types values from the JSONObjects that represent them. Turns all Numbers
	 * into BigDecimal for easier comparison. All fields and parameters must be run through this
	 * method.
	 *
	 * @param value
	 * @return null if value is a JSONObject, but not a complex type.
	 */
	public static Object parseValue(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Double) {
			return new BigDecimal((Double) value);
		} else if (value instanceof Long) {
			return new BigDecimal((Long) value);
		} else if (value instanceof Integer) {
			return new BigDecimal((Integer) value);
		} else if (value instanceof Float) {
			return new BigDecimal((Float) value);
		} else if (value instanceof Short) {
			return new BigDecimal((Short) value);
		} else if (value instanceof String) {
			return ((String) value).trim();
		} else if (value instanceof Apptentive.Version) {
			return value;
		} else if (value instanceof Apptentive.DateTime) {
			return value;
		} else if (value instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject) value;
			String typeName = jsonObject.optString(KEY_COMPLEX_TYPE);
			if (typeName != null) {
				try {
					if (Apptentive.Version.TYPE.equals(typeName)) {
						return new Apptentive.Version(jsonObject);
					} else if (Apptentive.DateTime.TYPE.equals(typeName)) {
						return new Apptentive.DateTime(jsonObject);
					} else {
						throw new RuntimeException(String.format("Error parsing complex parameter with unrecognized name: \"%s\"", typeName));
					}
				} catch (JSONException e) {
					throw new RuntimeException(String.format("Error parsing complex parameter: %s", Util.classToString(value)), e);
				}
			} else {
				throw new RuntimeException(String.format("Error: Complex type parameter missing \"%s\".", KEY_COMPLEX_TYPE));
			}
		}
		// All other values, such as Boolean and String should be returned unaltered.
		return value;
	}

}
