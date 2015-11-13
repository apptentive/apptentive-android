/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;

import com.apptentive.android.sdk.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class ClauseParser {

	public static Clause parse(String json) throws JSONException {
		Log.v("+ Parsing Interaction Criteria.");
		JSONObject root = new JSONObject(json);
		Clause ret = ClauseParser.parse(null, root);
		Log.v("+ Finished parsing Interaction Criteria.");
		return ret;
	}

	public static Clause parse(String key, Object value) throws JSONException {
		if (key == null) {
			// The Root object, and objects inside arrays should be treated as $and.
			key = LogicalOperator.$and.name();
		}
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
}
