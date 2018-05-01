/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.threading;

import java.util.LinkedList;
import java.util.Queue;

import static com.apptentive.android.sdk.util.RuntimeUtils.overrideStaticFinalField;

public class MockDispatchQueue extends DispatchQueue {
	private final boolean runImmediately;
	private Queue<DispatchTask> tasks;
	private boolean indentifyAsMainQueue = true;

	public MockDispatchQueue(boolean runImmediately) {
		super("Mock Queue");
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

	@Override
	public boolean isCurrent() {
		return true; // sure, why not? :)
	}

	public MockDispatchQueue setIndentifyAsMainQueue(boolean indentifyAsMainQueue) {
		this.indentifyAsMainQueue = indentifyAsMainQueue;
		return this;
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

	private static void overrideMainQueue(final MockDispatchQueue queue) {
		try {
			overrideStaticFinalField(findHolderClass(DispatchQueue.class.getDeclaredClasses()), "MAIN_QUEUE", queue);
			overrideStaticFinalField(DispatchQueue.class, "MAIN_QUEUE_CHECKER", new DispatchQueue.MainQueueChecker() {
				@Override
				public boolean isMainQueue() {
					return queue.indentifyAsMainQueue;
				}
			});
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	private static Class<?> findHolderClass(Class<?>[] classes) {
		for (Class<?> cls : classes) {
			if (cls.getSimpleName().equals("Holder")) {
				return cls;
			}
		}
		return null;
	}
}
