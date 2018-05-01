/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

public class InvocationException extends Exception {
	public InvocationException(String format, Object... args) {
		super(StringUtils.format(format, args));
	}

	public InvocationException(Throwable cause, String format, Object... args) {
		super(StringUtils.format(format, args), cause);
	}
}
