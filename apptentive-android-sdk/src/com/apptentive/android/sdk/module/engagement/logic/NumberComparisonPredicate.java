package com.apptentive.android.sdk.module.engagement.logic;

import org.json.JSONException;

/**
 * Used for performing predicate conditional logic on Numbers. Takes only Doubles, since all reasonably small numbers
 * can be cast to Double.
 * @author Sky Kelsey
 */
public class NumberComparisonPredicate extends ComparisonPredicate<Comparable> {
	public NumberComparisonPredicate(String queryName, Double value, Object condition) throws JSONException {
		super(queryName, value, condition);
	}
}
