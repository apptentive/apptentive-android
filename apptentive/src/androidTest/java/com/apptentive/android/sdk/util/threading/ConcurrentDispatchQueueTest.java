/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.threading;

import androidx.test.runner.AndroidJUnit4;
import com.apptentive.android.sdk.TestCaseBase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ConcurrentDispatchQueueTest extends TestCaseBase {
	private DispatchQueue dispatchQueue;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		dispatchQueue = DispatchQueue.createBackgroundQueue("Test Queue", DispatchQueueType.Concurrent);
	}

	@After
	public void tearDown() {
		dispatchQueue.stop();
		super.tearDown();
	}

	@Test
	public void testDispatch() {
		dispatchQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				sleep(500);
				addResult("task-1");
			}
		});
		dispatchQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				sleep(200);
				addResult("task-2");
			}
		});
		dispatchQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				addResult("task-3");
			}
		});
		sleep(1000); // give tasks a chance to finish
		assertResult("task-3", "task-2", "task-1"); // task should be executed concurrently
	}

	@Test
	public void testStoppingDispatchDelayed() {
		dispatchQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				addResult("task");
			}
		}, 100);
		dispatchQueue.stop();
		sleep(1000); // wait just for the case if the task still runs

		assertResult(); // no tasks should run
	}
}