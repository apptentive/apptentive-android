/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

/**
 * @author Sky Kelsey
 */
public enum LogicalOperator {
	$and,
	$or,
	$not,

	unknown;

	public static LogicalOperator parse(String name) {
		if (name != null) {
			name = name.trim();
			try {
				return LogicalOperator.valueOf(name);
			} catch (IllegalArgumentException e) {
				logException(e);
			}
		}
		return unknown;
	}
}
