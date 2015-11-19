/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Util;

import java.math.BigDecimal;

/**
 * @author Sky Kelsey
 */
public enum ConditionalOperator {
	$exists {
		@Override
		public boolean apply(Comparable first, Comparable second) {
			if (second == null) {
				return false;
			}
			if (!(second instanceof Boolean)) {
				return false;
			}
			boolean exists = first != null;
			return exists == (Boolean) second;
		}
	},

	$ne {
		@Override
		public boolean apply(Comparable first, Comparable second) {
			if (first == null || second == null) {
				return false;
			}
			if (first.getClass() != second.getClass()) {
				return false;
			}
			return first.compareTo(second) != 0;
		}
	},
	$eq {
		@Override
		public boolean apply(Comparable first, Comparable second) {
			if (first == null && second == null) {
				return true;
			}
			if (first == null || second == null) {
				return false;
			}
			if (first.getClass() != second.getClass()) {
				return false;
			}
			return first.compareTo(second) == 0;
		}
	},

	$lt {
		@Override
		public boolean apply(Comparable first, Comparable second) {
			if (first == null || second == null) {
				return false;
			}
			if (first.getClass() != second.getClass()) {
				return false;
			}
			return first.compareTo(second) < 0;
		}
	},
	$lte {
		@Override
		public boolean apply(Comparable first, Comparable second) {
			if (first == null || second == null) {
				return false;
			}
			if (first.getClass() != second.getClass()) {
				return false;
			}
			return first.compareTo(second) <= 0;
		}
	},
	$gte {
		@Override
		public boolean apply(Comparable first, Comparable second) {
			if (first == null || second == null) {
				return false;
			}
			if (first.getClass() != second.getClass()) {
				return false;
			}
			return first.compareTo(second) >= 0;
		}
	},
	$gt {
		@Override
		public boolean apply(Comparable first, Comparable second) {
			if (first == null || second == null) {
				return false;
			}
			if (first.getClass() != second.getClass()) {
				return false;
			}
			return first.compareTo(second) > 0;
		}
	},

	$contains {
		@Override
		public boolean apply(Comparable first, Comparable second) {
			if (first == null || second == null) {
				return false;
			}
			if (!(first instanceof String) || !(second instanceof String)) {
				return false;
			}
			return ((String) first).toLowerCase().contains(((String) second).toLowerCase());
		}
	},
	$starts_with {
		@Override
		public boolean apply(Comparable first, Comparable second) {
			if (!(first instanceof String) || !(second instanceof String)) {
				return false;
			}
			return ((String) first).toLowerCase().startsWith(((String) second).toLowerCase());
		}
	},
	$ends_with {
		@Override
		public boolean apply(Comparable first, Comparable second) {
			if (!(first instanceof String) || !(second instanceof String)) {
				return false;
			}
			return ((String) first).toLowerCase().endsWith(((String) second).toLowerCase());
		}
	},

	$before {
		@Override
		public boolean apply(Comparable first, Comparable second) {
			if (!(first instanceof Apptentive.DateTime)) {
				return false;
			}
			// The parameter for $before is an offset in seconds added to the current time.
			if (!(second instanceof BigDecimal)) {
				return false;
			}
			Double offset = ((BigDecimal) second).doubleValue();
			Double currentTime = Util.currentTimeSeconds();
			Apptentive.DateTime offsetDateTime = new Apptentive.DateTime(currentTime + offset);
			Log.v("      		- %s?", Util.classToString(offsetDateTime));
			return ((Apptentive.DateTime) first).compareTo(offsetDateTime) < 0;
		}
	},
	$after {
		@Override
		public boolean apply(Comparable first, Comparable second) {
			if (!(first instanceof Apptentive.DateTime)) {
				return false;
			}
			// The parameter for $after is an offset in seconds added to the current time.
			if (!(second instanceof BigDecimal)) {
				return false;
			}
			Double offset = ((BigDecimal) second).doubleValue();
			Double currentTime = Util.currentTimeSeconds();
			Apptentive.DateTime offsetDateTime = new Apptentive.DateTime(currentTime + offset);
			Log.v("      		- %s?", Util.classToString(offsetDateTime));
			return ((Apptentive.DateTime) first).compareTo(offsetDateTime) > 0;
		}
	},

	unknown {
		@Override
		public boolean apply(Comparable first, Comparable second) {
			return false;
		}
	};

	public static ConditionalOperator parse(String name) {
		if (name != null) {
			try {
				return ConditionalOperator.valueOf(name);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(String.format("Unrecognized ConditionalOperator: %s", name), e);
			}
		}
		return unknown;
	}

	public abstract boolean apply(Comparable first, Comparable second);
}
