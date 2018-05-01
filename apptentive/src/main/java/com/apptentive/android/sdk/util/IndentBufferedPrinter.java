/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

public class IndentBufferedPrinter extends IndentPrinter {
	private final StringBuilder buffer;

	public IndentBufferedPrinter() {
		buffer = new StringBuilder();
	}

	@Override
	protected void printInternal(String message) {
		if (buffer.length() > 0) {
			buffer.append('\n');
		}
		buffer.append(message);
	}

	@Override
	public String toString() {
		return buffer.toString();
	}
}
