/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.Util;

import static com.apptentive.android.sdk.ApptentiveLogTag.INTERACTIONS;

/**
 * @author Sky Kelsey
 */
public class ConditionalTest {

	public ConditionalOperator operator;
	public Comparable parameter;

	public ConditionalTest(ConditionalOperator operator, Object parameter) {
		ApptentiveLog.v(INTERACTIONS, "      + ConditionalTest: %s: %s", operator.name(), Util.classToString(parameter));
		this.operator = operator;
		if (parameter != null && !(parameter instanceof Comparable)) {
			throw new IllegalArgumentException(String.format("Encountered non-Comparable parameter: %s", Util.classToString(parameter)));
		}
		this.parameter = (Comparable) parameter;
	}

}
