/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;


import com.apptentive.android.sdk.ApptentiveLog;
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

	String fieldName;
	List<ConditionalTest> conditionalTests;

	public ConditionalClause(String field, Object inputValue) {
		this.fieldName = field.trim();
		conditionalTests = new ArrayList<ConditionalTest>();

		ApptentiveLog.v("    + ConditionalClause for query: \"%s\"", fieldName);
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
	 * @return
	 */
	@Override
	public boolean evaluate(FieldManager fieldManager) {
		ApptentiveLog.v("    - %s", fieldName);
		Comparable fieldValue = fieldManager.getValue(fieldName);
		for (ConditionalTest test : conditionalTests) {
			ApptentiveLog.v("      - %s %s %s?", Util.classToString(fieldValue), test.operator, Util.classToString(test.parameter));
			if (!test.operator.apply(fieldValue, test.parameter)) {
				return false;
			}
		}
		return true;
	}
}
