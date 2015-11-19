/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;

import android.content.Context;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class ConditionalClause implements Clause {

	private static final String KEY_COMPLEX_TYPE = "_type";

	String query;
	List<ConditionalTest> conditionalTests;

	public ConditionalClause(String query, Object inputValue) {
		this.query = query;
		conditionalTests = new ArrayList<ConditionalTest>();

		Log.v("    + ConditionalClause for query: \"%s\"", query);
		if (inputValue instanceof JSONObject && !isComplexType((JSONObject) inputValue)) {
			conditionalTests = getConditions((JSONObject) inputValue);
		} else {
			conditionalTests.add(new ConditionalTest(ConditionalOperator.$eq, ClauseParser.parseValue(inputValue)));
		}
	}

	private List<ConditionalTest> getConditions(JSONObject conditionObject) {
		List<ConditionalTest> conditionalTests = new ArrayList<ConditionalTest>();
		Iterator<String> operators = conditionObject.keys();
		while (operators.hasNext()) {
			String operator = operators.next();
			Object value = null;
			if (!conditionObject.isNull(operator)) {
				value = ClauseParser.parseValue(conditionObject.opt(operator));
			}
			conditionalTests.add(new ConditionalTest(ConditionalOperator.parse(operator), value));
		}
		return conditionalTests;
	}

	private boolean isComplexType(JSONObject jsonObject) {
		return jsonObject != null && !jsonObject.isNull(KEY_COMPLEX_TYPE);
	}

	/**
	 * The test in this conditional clause are implicitly ANDed together, so return false if any of them is false, and continue the loop for each test that is true;
	 *
	 * @param context
	 * @return
	 */
	@Override
	public boolean evaluate(Context context) {
		Log.v("    - %s", query);
		Comparable field = FieldManager.getValue(context, query);
		for (ConditionalTest test : conditionalTests) {
			Log.v("      - %s %s %s?", Util.classToString(field), test.operator, Util.classToString(test.parameter));
			if (!test.operator.apply(field, test.parameter)) {
				return false;
			}
		}
		return true;
	}
}
