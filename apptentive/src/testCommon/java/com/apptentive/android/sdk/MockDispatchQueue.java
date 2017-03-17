/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.threading;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.Queue;

public class MockDispatchQueue extends DispatchQueue {
	private final boolean runImmediately;
	private Queue<DispatchTask> tasks;

	public MockDispatchQueue(boolean runImmediately) {
		this.runImmediately = runImmediately;
		this.tasks = new LinkedList<>();
	}

	@Override
	protected void dispatch(DispatchTask task, long delayMillis) {
		if (runImmediately) {
			task.run();
		} else {
			tasks.add(task);
		}
	}

	@Override
	public void stop() {
		tasks.clear();
	}

	public void dispatchTasks() {
		while (tasks.size() > 0) {
			tasks.poll().run();
		}
	}

	public static MockDispatchQueue overrideMainQueue(boolean runImmediately) {
		MockDispatchQueue queue = new MockDispatchQueue(runImmediately);
		overrideMainQueue(queue);
		return queue;
	}

	private static void overrideMainQueue(DispatchQueue queue) {
		try {
			Class<?> holderClass = DispatchQueue.class.getDeclaredClasses()[0];
			Field instanceField = holderClass.getDeclaredField("MAIN_QUEUE");
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
