/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;

import android.content.Context;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONObject;

import java.math.BigDecimal;
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
		Log.v("      - => %s", Util.classToString(field));
		for (ConditionalTest test : conditionalTests) {
			switch (test.operator) {
				case $exists: {
					Log.v("      - %s %s?", test.operator, Util.classToString(test.parameter));
					if (test.parameter == null) {
						return false;
					}
					if (!(test.parameter instanceof Boolean)) {
						return false;
					}
					boolean exists = FieldManager.exists(context, query);
					boolean parameter = (Boolean) test.parameter;
					if (exists == parameter) {
						continue;
					} else {
						return false;
					}
				}
				case $eq:
					Log.v("      - %s %s?", test.operator, Util.classToString(test.parameter));
					if (field == null && test.parameter == null) {
						continue;
					}
					if (field == null || test.parameter == null) {
						return false;
					}
					if (field.getClass() != test.parameter.getClass()) {
						return false;
					}
					if (field.compareTo(test.parameter) == 0) {
						continue;
					} else {
						return false;
					}
				case $ne:
					Log.v("      - %s %s?", test.operator, Util.classToString(test.parameter));
					if (field == null || test.parameter == null) {
						return false;
					}
					if (field.getClass() != test.parameter.getClass()) {
						return false;
					}
					if (field.compareTo(test.parameter) != 0) {
						continue;
					} else {
						return false;
					}
				case $lt:
					Log.v("      - %s %s?", test.operator, Util.classToString(test.parameter));
					if (field == null || test.parameter == null) {
						return false;
					}
					if (field.getClass() != test.parameter.getClass()) {
						return false;
					}
					if (field.compareTo(test.parameter) < 0) {
						continue;
					} else {
						return false;
					}
				case $lte:
					Log.v("      - %s %s?", test.operator, Util.classToString(test.parameter));
					if (field == null || test.parameter == null) {
						return false;
					}
					if (field.getClass() != test.parameter.getClass()) {
						return false;
					}
					if (field.compareTo(test.parameter) <= 0) {
						continue;
					} else {
						return false;
					}
				case $gte:
					Log.v("      - %s %s?", test.operator, Util.classToString(test.parameter));
					if (field == null || test.parameter == null) {
						return false;
					}
					if (field.getClass() != test.parameter.getClass()) {
						return false;
					}
					if (field.compareTo(test.parameter) >= 0) {
						continue;
					} else {
						return false;
					}
				case $gt:
					Log.v("      - %s %s?", test.operator, Util.classToString(test.parameter));
					if (field == null || test.parameter == null) {
						return false;
					}
					if (field.getClass() != test.parameter.getClass()) {
						return false;
					}
					if (field.compareTo(test.parameter) > 0) {
						continue;
					} else {
						return false;
					}
				case $starts_with:
					Log.v("      - %s %s?", test.operator, Util.classToString(test.parameter));
					if (!(field instanceof String) || !(test.parameter instanceof String)) {
						return false;
					}
					if (((String) field).toLowerCase().startsWith(((String) test.parameter).toLowerCase())) {
						continue;
					} else {
						return false;
					}
				case $ends_with:
					Log.v("      - %s %s?", test.operator, Util.classToString(test.parameter));
					if (!(field instanceof String) || !(test.parameter instanceof String)) {
						return false;
					}
					if (((String) field).toLowerCase().endsWith(((String) test.parameter).toLowerCase())) {
						continue;
					} else {
						return false;
					}
				case $contains:
					Log.v("      - %s %s?", test.operator, Util.classToString(test.parameter));
					if (field == null || test.parameter == null) {
						return false;
					}
					if (!(field instanceof String) || !(test.parameter instanceof String)) {
						return false;
					}
					if (((String) field).toLowerCase().contains(((String) test.parameter).toLowerCase())) {
						continue;
					} else {
						return false;
					}
				case $before: {
					// The parameter for $before is an offset in seconds added to the current time.
					if (!(test.parameter instanceof BigDecimal)) {
						return false;
					}
					if (!(field instanceof Apptentive.DateTime)) {
						return false;
					}
					Double offset = ((BigDecimal) test.parameter).doubleValue();
					Double currentTime = Util.currentTimeSeconds();
					Apptentive.DateTime offsetDateTime = new Apptentive.DateTime(currentTime + offset);
					Log.v("      - %s %s?", test.operator, Util.classToString(offsetDateTime));
					if (((Apptentive.DateTime) field).compareTo(offsetDateTime) < 0) {
						continue;
					} else {
						return false;
					}
				}
				case $after: {
					// The parameter for $after is an offset in seconds added to the current time.
					if (!(test.parameter instanceof BigDecimal)) {
						return false;
					}
					if (!(field instanceof Apptentive.DateTime)) {
						return false;
					}
					Double offset = ((BigDecimal) test.parameter).doubleValue();
					Double currentTime = Util.currentTimeSeconds();
					Apptentive.DateTime offsetDateTime = new Apptentive.DateTime(currentTime + offset);
					Log.v("      - %s %s?", test.operator, Util.classToString(offsetDateTime));
					if (((Apptentive.DateTime) field).compareTo(offsetDateTime) > 0) {
						continue; // The compiler says this is unnecessary. But you just know that if you remove it, you won't remember to add it again when you add support for another operator.
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}
}
