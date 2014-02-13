package com.apptentive.android.sdk.module.engagement.logic;

import org.json.JSONException;

/**
 * Used for performing predicate conditional logic on Booleans.
 *
 * @author Sky Kelsey
 */
public class BooleanComparisonPredicate extends ComparisonPredicate<Comparable> {
	public BooleanComparisonPredicate(String queryName, Boolean value, Object condition) throws JSONException {
		super(queryName, value, condition);
	}
}
