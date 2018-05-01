/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;


import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.IndentPrinter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.apptentive.android.sdk.ApptentiveLogTag.INTERACTIONS;

/**
 * @author Sky Kelsey
 */
public class LogicalClause implements Clause {

	private final String operatorName;
	private final LogicalOperator operator;
	private final List<Clause> children;

	protected LogicalClause(String key, Object value) throws JSONException {
		operatorName = key.trim();
		operator = LogicalOperator.parse(operatorName);
		children = new ArrayList<>();
		ApptentiveLog.v(INTERACTIONS, "  + LogicalClause of type \"%s\"", operatorName);
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
			ApptentiveLog.w(INTERACTIONS, "Unrecognized LogicalClause: %s", value.toString());
		}
	}

	@Override
	public boolean evaluate(FieldManager fieldManager, IndentPrinter printer) {
		// to compress logs we 'inline' single $and operators
		boolean printOperator = !LogicalOperator.$and.equals(operator) || children.size() > 1;
		if (printOperator) {
			printer.print("- %s:", operator.name());
			printer.startBlock();
		}
		try {
			return evaluateOperator(fieldManager, printer);
		} finally {
			if (printOperator) {
				printer.endBlock();
			}
		}
	}

	private boolean evaluateOperator(FieldManager fieldManager, IndentPrinter printer) {
		if (operator == LogicalOperator.$and) {
			for (Clause clause : children) {
				boolean ret = clause.evaluate(fieldManager, printer);
				if (!ret) {
					return false;
				}
			}
			return true;
		}

		if (operator == LogicalOperator.$or) {
			for (Clause clause : children) {
				boolean ret = clause.evaluate(fieldManager, printer);
				if (ret) {
					return true;
				}
			}
			return false;
		}

		if (operator == LogicalOperator.$not) {
			if (children.size() != 1) {
				throw new IllegalArgumentException("$not condition must have exactly one child, has ." + children.size());
			}
			Clause clause = children.get(0);
			boolean ret = clause.evaluate(fieldManager, printer);
			return !ret;
		}

		// Unsupported
		ApptentiveLog.v(INTERACTIONS, "Unsupported operation: \"%s\" => false", operatorName);
		ApptentiveLog.v(INTERACTIONS, "  - </%s>", operator.name());
		return false;
	}
}
