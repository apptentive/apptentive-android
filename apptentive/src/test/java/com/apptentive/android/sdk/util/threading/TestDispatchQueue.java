/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.threading;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class TestDispatchQueue extends DispatchQueue {
	@Override
	protected void dispatch(DispatchTask runnable) {
		runnable.run();
	}

	@Override
	public void stop() {
		// do nothing
	}

	public static void overrideMainQueue() {
		overrideMainQueue(new TestDispatchQueue());
	}

	private static void overrideMainQueue(DispatchQueue queue) {
		try {
			Class<?> holderClass = DispatchQueue.class.getDeclaredClasses()[0];
			Field instanceField = holderClass.getDeclaredField("INSTANCE");
			instanceField.setAccessible(true);

			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(instanceField, instanceField.getModifiers() & ~Modifier.FINAL);

			instanceField.set(null, queue);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
}
