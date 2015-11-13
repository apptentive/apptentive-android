/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;

/**
 * @author Sky Kelsey
 */
public enum ConditionalOperator {
	$exists,

	$ne,
	$eq,

	$lt,
	$lte,
	$gte,
	$gt,

	$contains,
	$starts_with,
	$ends_with,

	$before,
	$after,

	unknown;

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

}
