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

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class ConditionalClause implements Clause {
	String query;
	List<ConditionalTest> conditionalTests;

	public ConditionalClause(String query, Object inputValue) {
		this.query = query;
		conditionalTests = new ArrayList<ConditionalTest>();

		Log.v("    + ConditionalClause for query: \"%s\"", query);
		if (inputValue instanceof JSONObject && !isComplexType((JSONObject) inputValue)) {
			conditionalTests = getConditions((JSONObject) inputValue);
		} else {
			conditionalTests.add(new ConditionalTest(ConditionalOperator.$eq, parseValue(inputValue)));
		}
	}

	private List<ConditionalTest> getConditions(JSONObject conditionObject) {
		List<ConditionalTest> conditionalTests = new ArrayList<ConditionalTest>();
		Iterator<String> operators = conditionObject.keys();
		while (operators.hasNext()) {
			String operator = operators.next();
			Object value = parseValue(conditionObject.opt(operator));
			conditionalTests.add(new ConditionalTest(ConditionalOperator.parse(operator), value));
		}
		return conditionalTests;
	}

	/**
	 * Constructs complex types values from the JSONObjects that represent them. Turns all Numbers into BigDecimal for easier comparison.
	 *
	 * @param value
	 * @return null if value is a JSONObject, but not a complex type.
	 */
	private static Object parseValue(Object value) {
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
		} else if (value instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject) value;
			String typeName = jsonObject.optString("_type");
			if (typeName != null) {
				try {
					if (Apptentive.Version.TYPE.equals(typeName)) {
						return new Apptentive.Version(jsonObject.toString());
					} else if (Apptentive.DateTime.TYPE.equals(typeName)) {
						return new Apptentive.DateTime(jsonObject.toString());
					} else {
						throw new RuntimeException(String.format("Error parsing complex parameter with unrecognized name: \"%s\"", typeName));
					}
				} catch (JSONException e) {
					throw new RuntimeException(String.format("Error parsing complex parameter with name: \"%s\", and value: \"%s\"" + typeName, value), e);
				}
			} else {
				throw new RuntimeException("Error: Complex type parameter missing \"_type\".");
			}
		}
		// All other values, such as Boolean and String should be returned unaltered.
		return value;
	}

	private boolean isComplexType(JSONObject jsonObject) {
		return jsonObject != null && !jsonObject.isNull("_type");
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
		Log.v("      - => %s(%s)", field == null ? "null" : field.getClass().getSimpleName(), field);
		for (ConditionalTest test : conditionalTests) {
			switch (test.operator) {
				case $exists: {
					Log.v("      - %s %s?", test.operator, test.parameter);
					if (test.parameter == null) {
						return false;
					}
					if (!(test.parameter instanceof Boolean)) {
						throw new IllegalArgumentException(String.format("%s operator passed parameter of type %s", test.operator.name(), test.parameter.getClass().getSimpleName()));
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
					Log.v("      - %s %s(%s)?", test.operator, test.parameter.getClass().getSimpleName(), test.parameter);
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
					Log.v("      - %s %s?", test.operator, test.parameter);
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
					Log.v("      - %s %s?", test.operator, test.parameter);
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
					Log.v("      - %s %s?", test.operator, test.parameter);
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
					Log.v("      - %s %s?", test.operator, test.parameter);
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
					Log.v("      - %s %s?", test.operator, test.parameter);
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
					Log.v("      - %s %s?", test.operator, test.parameter);
					if (!(field instanceof String) || !(test.parameter instanceof String)) {
						return false;
					}
					if (((String) field).toLowerCase().startsWith(((String) test.parameter).toLowerCase())) {
						continue;
					} else {
						return false;
					}
				case $ends_with:
					Log.v("      - %s %s?", test.operator, test.parameter);
					if (!(field instanceof String) || !(test.parameter instanceof String)) {
						return false;
					}
					if (((String) field).toLowerCase().endsWith(((String) test.parameter).toLowerCase())) {
						continue;
					} else {
						return false;
					}
				case $contains:
					Log.v("      - %s %s?", test.operator, test.parameter);
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
					Log.v("      - %s %s(%s)?", test.operator, offsetDateTime.getClass().getSimpleName(), offsetDateTime);
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
					Log.v("      - %s %s(%s)?", test.operator, offsetDateTime.getClass().getSimpleName(), offsetDateTime);
					if (((Apptentive.DateTime) field).compareTo(offsetDateTime) > 0) {
						continue;
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}
}
