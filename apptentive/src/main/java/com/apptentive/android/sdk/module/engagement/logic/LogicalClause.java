/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;


import com.apptentive.android.sdk.ApptentiveLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class LogicalClause implements Clause {

	protected String operatorName;
	protected LogicalOperator operator;
	protected List<Clause> children;

	protected LogicalClause(String key, Object value) throws JSONException {
		operatorName = key.trim();
		operator = LogicalOperator.parse(operatorName);
		children = new ArrayList<Clause>();
		ApptentiveLog.v("  + LogicalClause of type \"%s\"", operatorName);
		if (value instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) value;
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject child = (JSONObject) jsonArray.get(i);
				children.add(ClauseParser.parse(null, child));
			}
		} else if (value instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject) value;
			Iterator<String> it = jsonObject.keys();
			while (it.hasNext()) {
				String currentKey = it.next();
				if (!jsonObject.isNull(currentKey)) {
					children.add(ClauseParser.parse(currentKey, jsonObject.get(currentKey)));
				} else {
					children.add(ClauseParser.parse(currentKey, null));
				}
			}
		} else {
			ApptentiveLog.w("Unrecognized LogicalClause: %s", value.toString());
		}
	}

	@Override
	public boolean evaluate(FieldManager fieldManager) {
		ApptentiveLog.v("  - <%s>", operator.name());
		if (operator == LogicalOperator.$and) {
			for (Clause clause : children) {
				boolean ret = clause.evaluate(fieldManager);
				ApptentiveLog.v("        - => %b", ret);
				if (!ret) {
					ApptentiveLog.v("  - </%s>", operator.name());
					return false;
				}
			}
			ApptentiveLog.v("  - </%s>", operator.name());
			return true;
		} else if (operator == LogicalOperator.$or) {
			for (Clause clause : children) {
				boolean ret = clause.evaluate(fieldManager);
				ApptentiveLog.v("        - => %b", ret);
				if (ret) {
					ApptentiveLog.v("  - </%s>", operator.name());
					return true;
				}
			}
			ApptentiveLog.v("  - </%s> => false", operator.name());
			return false;
		} else if (operator == LogicalOperator.$not) {
			if (children.size() != 1) {
				throw new IllegalArgumentException("$not condition must have exactly one child, has ." + children.size());
			}
			Clause clause = children.get(0);
			boolean ret = clause.evaluate(fieldManager);
			ApptentiveLog.v("        - => %b", ret);
			ApptentiveLog.v("  - </%s>", operator.name());
			return !ret;
		} else {
			// Unsupported
			ApptentiveLog.v("Unsupported operation: \"%s\" => false", operatorName);
			ApptentiveLog.v("  - </%s>", operator.name());
			return false;
		}

	}

}
