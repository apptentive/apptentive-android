package com.apptentive.android.sdk.module.engagement.logic;

import android.content.Context;
import com.apptentive.android.sdk.Log;
import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public abstract class Predicate {

	protected Predicate parent;

	public abstract boolean apply(Context context);

	public static Predicate parse(Context context, String key, Object value) throws JSONException {
		if (key == null) {
			// This is the root, so it must be an AND.
			return new CombinationPredicate(context, Operation.$and, value);
		} else {
			Operation op = Operation.parse(key);
			switch (op) {
				case $or:
					return new CombinationPredicate(context, Operation.$or, value);
				case $and:
					return new CombinationPredicate(context, Operation.$and, value);
				default: // All other keys meant this is a ComparisonPredicate.
					return ComparisonPredicateFactory.generatePredicate(context, key, value);
			}
		}
	}

	public enum Operation {
		$and,
		$or,
		$lt,
		$lte,
		$ne,
		$eq,
		$gte,
		$gt,
		unknown;

		public static Operation parse(String name) {
			try {
				return Operation.valueOf(name);
			} catch (IllegalArgumentException e) {
				// This will happen on old clients if we extend the logic syntax, so don't log.
			}
			return unknown;
		}
	}
}
