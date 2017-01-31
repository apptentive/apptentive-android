/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.threading;

import com.apptentive.android.sdk.TestCaseBase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by alementuev on 1/30/17.
 */
public class DispatchQueueTest extends TestCaseBase {

	@Before
	public void setUp() {
		overrideMainQueue(true);
	}

	@Test
	public void testSchedulingTasks() {
		DispatchTask task = new DispatchTask() {
			@Override
			protected void execute() {
				addResult("executed");
			}
		};

		DispatchQueue.mainQueue().dispatchAsync(task);
		DispatchQueue.mainQueue().dispatchAsync(task);
		dispatchTasks();

		assertResult("executed", "executed");
	}

	@Test
	public void testSchedulingTasksOnce() {

		DispatchTask task = new DispatchTask() {
			@Override
			protected void execute() {
				addResult("executed");
			}
		};

		DispatchQueue.mainQueue().dispatchAsyncOnce(task);
		DispatchQueue.mainQueue().dispatchAsyncOnce(task);
		dispatchTasks();

		assertResult("executed");

		DispatchQueue.mainQueue().dispatchAsyncOnce(task);
		DispatchQueue.mainQueue().dispatchAsyncOnce(task);
		dispatchTasks();

		assertResult("executed");

		DispatchQueue.mainQueue().dispatchAsync(task);
		DispatchQueue.mainQueue().dispatchAsync(task);
		dispatchTasks();

		assertResult("executed", "executed");
	}

	@Test
	public void testSchedulingTasksWithException() {

		DispatchQueue.mainQueue().dispatchAsyncOnce(new DispatchTask() {
			@Override
			protected void execute() {
				addResult("task-1");
			}
		});
		DispatchQueue.mainQueue().dispatchAsyncOnce(new DispatchTask() {
			@Override
			protected void execute() {
				throw new RuntimeException(); // throwing an exception should not break the queue
			}
		});
		DispatchQueue.mainQueue().dispatchAsyncOnce(new DispatchTask() {
			@Override
			protected void execute() {
				addResult("task-2");
			}
		});
		dispatchTasks();

		assertResult("task-1", "task-2");
	}
}