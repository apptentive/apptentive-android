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
	private final boolean dispatchManually;
	private Queue<DispatchTask> tasks;

	public MockDispatchQueue(boolean dispatchManually) {
		this.dispatchManually = dispatchManually;
		this.tasks = new LinkedList<>();
	}

	@Override
	protected void dispatch(DispatchTask task, long delayMillis) {
		if (dispatchManually) {
			tasks.add(task);
		} else {
			task.run();
		}
	}

	@Override
	public void stop() {
		tasks.clear();
	}

	public void dispatchTasks() {
		for (DispatchTask task : tasks) {
			task.run();
		}
		tasks.clear();
	}

	public static MockDispatchQueue overrideMainQueue(boolean dispatchTasksManually) {
		MockDispatchQueue queue = new MockDispatchQueue(dispatchTasksManually);
		overrideMainQueue(queue);
		return queue;
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
