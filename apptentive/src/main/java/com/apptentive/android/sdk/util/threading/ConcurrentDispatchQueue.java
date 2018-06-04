/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.threading;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrent queue implementation where each task is dispatched on a separate thread
 */
class ConcurrentDispatchQueue extends DispatchQueue implements ThreadFactory {
	/*
	 * Gets the number of available cores
	 * (not always the same as the maximum number of cores)
	 */
	private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

	/**
	 * Thread pool executor for scheduling tasks
	 */
	private final ScheduledThreadPoolExecutor threadPoolExecutor;

	/** The number of the next thread in the pool */
	private final AtomicInteger threadNumber;

	ConcurrentDispatchQueue(String name) {
		super(name);
		this.threadPoolExecutor = new ScheduledThreadPoolExecutor(NUMBER_OF_CORES, this);
		this.threadPoolExecutor.setMaximumPoolSize(NUMBER_OF_CORES);
		this.threadNumber = new AtomicInteger(1);
	}

	@Override
	protected void dispatch(DispatchTask task, long delayMillis) {
		if (delayMillis > 0) {
			threadPoolExecutor.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
		} else {
			threadPoolExecutor.execute(task);
		}
	}

	@Override
	public void stop() {
		threadPoolExecutor.shutdownNow();
	}

	@Override
	public boolean isCurrent() {
		return false; // TODO: figure it out how to check if the queue is current
	}

	//region Thread factory

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, getName() + " (thread-" + threadNumber.getAndIncrement() + ")");
	}

	//endregion
}
