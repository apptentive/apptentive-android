package com.apptentive.android.sdk.module.engagement.logic;

import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class StringComparisonPredicate extends ComparisonPredicate<String> {
	public StringComparisonPredicate(String queryName, String value, Object condition) throws JSONException {
		super(queryName, value, condition);
	}
}
