/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;

import java.math.BigDecimal;

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

		@Override
		public String description(String fieldName, Comparable first, Comparable second) {
			return StringUtils.format("%s ('%s') exists", fieldName, first);
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
			if (first instanceof String && second instanceof String) {
				return !((String) first).toLowerCase().equals(((String) second).toLowerCase());
			}
			return first.compareTo(second) != 0;
		}

		@Override
		public String description(String fieldName, Comparable first, Comparable second) {
			return StringUtils.format("%s ('%s') not equal to '%s'", fieldName, first, second);
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
			if (first instanceof String && second instanceof String) {
				return ((String) first).toLowerCase().equals(((String) second).toLowerCase());
			}
			return first.compareTo(second) == 0;
		}

		@Override
		public String description(String fieldName, Comparable first, Comparable second) {
			return StringUtils.format("%s ('%s') equal to '%s'", fieldName, first, second);
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

		@Override
		public String description(String fieldName, Comparable first, Comparable second) {
			return StringUtils.format("%s (%s) less than %s", fieldName, first, second);
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

		@Override
		public String description(String fieldName, Comparable first, Comparable second) {
			return StringUtils.format("%s ('%s') is less than or equal to '%s'", fieldName, first, second);
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

		@Override
		public String description(String fieldName, Comparable first, Comparable second) {
			return StringUtils.format("%s ('%s') is greater than or equal to '%s'", fieldName, first, second);
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

		@Override
		public String description(String fieldName, Comparable first, Comparable second) {
			return StringUtils.format("%s ('%s') greater than '%s'", fieldName, first, second);
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

		@Override
		public String description(String fieldName, Comparable first, Comparable second) {
			return StringUtils.format("%s ('%s') contains '%s'", fieldName, first, second);
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

		@Override
		public String description(String fieldName, Comparable first, Comparable second) {
			return StringUtils.format("%s ('%s') starts with '%s'", fieldName, first, second);
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

		@Override
		public String description(String fieldName, Comparable first, Comparable second) {
			return StringUtils.format("%s ('%s') ends with '%s'", fieldName, first, second);
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
			return ((Apptentive.DateTime) first).compareTo(offsetDateTime) < 0;
		}

		@Override
		public String description(String fieldName, Comparable first, Comparable second) {
			if (!(second instanceof BigDecimal)) {
				return StringUtils.format("%s ('%s') before date '%s'", fieldName, toPrettyDate(first), toPrettyDate(second));
			}

			Double offset = ((BigDecimal) second).doubleValue();
			Double currentTime = Util.currentTimeSeconds();
			return StringUtils.format("%s ('%s') before date '%s'", fieldName, toPrettyDate(first), toPrettyDate(currentTime + offset));
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
			return ((Apptentive.DateTime) first).compareTo(offsetDateTime) > 0;
		}

		@Override
		public String description(String fieldName, Comparable first, Comparable second) {
			if (!(second instanceof BigDecimal)) {
				return StringUtils.format("%s ('%s') after date '%s'", fieldName, toPrettyDate(first), toPrettyDate(second));
			}

			Double offset = ((BigDecimal) second).doubleValue();
			Double currentTime = Util.currentTimeSeconds();
			return StringUtils.format("%s ('%s') after date '%s'", fieldName, toPrettyDate(first), toPrettyDate(currentTime + offset));
		}
	},

	unknown {
		@Override
		public boolean apply(Comparable first, Comparable second) {
			return false;
		}


		@Override
		public String description(String fieldName, Comparable first, Comparable second) {
			return StringUtils.format("Unknown field '%s'", fieldName);
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
	public abstract String description(String fieldName, Comparable first, Comparable second);

	private static String toPrettyDate(Object value) {
		if (value instanceof Apptentive.DateTime) {
			Apptentive.DateTime date = (Apptentive.DateTime) value;
			return StringUtils.toPrettyDate(date.getDateTime());
		}

		return StringUtils.toString(value);
	}
}
