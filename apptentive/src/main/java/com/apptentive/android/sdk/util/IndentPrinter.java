/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import static com.apptentive.android.sdk.debug.Assert.assertTrue;

public abstract class IndentPrinter {
	private static final String INDENT = "  ";
	private final StringBuilder indentBuffer;

	public IndentPrinter() {
		indentBuffer = new StringBuilder();
	}

	protected abstract void printInternal(String message);

	public IndentPrinter print(String format, Object... args) {
		String message = indentBuffer + StringUtils.format(format, args);
		printInternal(message);
		return this;
	}

	public IndentPrinter startBlock() {
		indentBuffer.append(INDENT);
		return this;
	}

	public IndentPrinter endBlock() {
		assertTrue(indentBuffer.length() >= INDENT.length());
		if (indentBuffer.length() >= INDENT.length()) {
			indentBuffer.setLength(indentBuffer.length() - INDENT.length());
		}
		return this;
	}

	public static final IndentPrinter NULL = new IndentPrinter() {
		@Override
		public IndentPrinter print(String format, Object... args) {
			// don't create any unnecessary objects here
			return this;
		}

		@Override
		protected void printInternal(String message) {
		}

		@Override
		public IndentPrinter startBlock() {
			return this;
		}

		@Override
		public IndentPrinter endBlock() {
			return this;
		}
	};
}
