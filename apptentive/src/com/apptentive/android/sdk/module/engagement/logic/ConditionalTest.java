/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;

import com.apptentive.android.sdk.Log;

/**
 * @author Sky Kelsey
 */
public class ConditionalTest {

	public ConditionalOperator operator;
	public Comparable parameter;

	public ConditionalTest(ConditionalOperator operator, Object parameter) {
		this.operator = operator;
		if (!(parameter instanceof Comparable)) {
			throw new IllegalArgumentException(String.format("Encountered non-Comparable parameter of type: %s, and value: %s", parameter.getClass().getSimpleName(), parameter.toString()));
		}
		this.parameter = (Comparable) parameter;
		Log.v("      + ConditionalTest: %s: %s(%s)", operator.name(), parameter.getClass().getSimpleName(), parameter instanceof String ? String.format("\"%s\"", parameter) : parameter.toString());
	}

}
