package com.apptentive.android.sdk.module.engagement.logic;

import android.content.Context;
import com.apptentive.android.sdk.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class ComparisonPredicate<T extends Comparable> extends Predicate {

	protected String queryName;
	protected T value;
	protected List<Condition<T>> conditions;

	public ComparisonPredicate(String queryName, T value, Object condition) throws JSONException {
		this.queryName = queryName;
		this.value = value;
		conditions = new ArrayList<Condition<T>>();
		T fixedCondition = null;
		if (condition instanceof JSONObject) {
			JSONObject conditionJson = (JSONObject) condition;
			// This is an object. It may contain multiple comparison operations, so unroll  it and add them all in.
			@SuppressWarnings("unchecked")
			Iterator<String> it = (Iterator<String>) conditionJson.keys();
			while (it.hasNext()) {
				String conditionKey = it.next();
				Operation operation = Operation.valueOf(conditionKey);
				fixedCondition = ensureType(value, conditionJson.get(conditionKey));
				conditions.add(new Condition<T>(operation, (T) fixedCondition));
			}
		} else {
			// If it's a literal, then it has to be an EQUALITY operation. The others are always wrapped in a JSONObject.
			fixedCondition = ensureType(value, condition);
			conditions.add(new Condition<T>(Operation.$eq, (T) fixedCondition));
		}
	}

	/**
	 * Ensures that all operands are coerced to Doubles if the value is a Double, Strings if the value is a String, and
	 * Boolean if the value is a Boolean.
	 */
	private T ensureType(T value, Object condition) {
		if (!value.getClass().equals(condition.getClass())) {
			if (value instanceof String) {
				if (condition instanceof String) {
					return (T) condition;
				} else {
					return (T) condition.toString();
				}
			} else if (value instanceof Number) {
				if (condition instanceof Double) {
					return (T) condition;
				} else if (condition instanceof Integer) {
					return (T) (Double) ((Integer) condition).doubleValue();
				} else if (condition instanceof Long) {
					return (T) (Double) ((Long) condition).doubleValue();
				}
			} else if (value instanceof Boolean) {
				if (condition instanceof Boolean) {
					return (T) condition;
				}
			}
		} else {
			return (T) condition;
		}
		return null;
	}

	/**
	 * Ensures all Integers are actually treated as Longs. This is because JSONObjects cant' make a distinction.
	 *
	 * @param input
	 * @return
	 */
	private Comparable castIntegertoLong(Comparable input) {
		if (input instanceof Integer) {
			return (long) (Integer) input;
		}
		return input;
	}

	@Override
	public boolean apply(Context context) {
		Log.v("Comparison Predicate: %s", queryName);
		for (Condition condition : conditions) {
			Log.v("-- Compare: %s %s %s", getLoggableValue(value), condition.operation, getLoggableValue(condition.operand));
			switch (condition.operation) {
				case $gt:
					if (!(value.compareTo(condition.operand) > 0)) {
						return false;
					}
					break;
				case $gte:
					if (!(value.compareTo(condition.operand) >= 0)) {
						return false;
					}
					break;
				case $eq:
					if (!value.equals(condition.operand)) {
						return false;
					}
					break;
				case $ne:
					if (value.equals(condition.operand)) {
						return false;
					}
					break;
				case $lte:
					if (!(value.compareTo(condition.operand) <= 0)) {
						return false;
					}
					break;
				case $lt:
					if (!(value.compareTo(condition.operand) < 0)) {
						return false;
					}
					break;
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
}
