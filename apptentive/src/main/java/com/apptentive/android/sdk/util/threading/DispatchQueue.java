/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.threading;

import android.os.Looper;

/**
 * A class representing dispatch queue where <code>{@link Runnable}</code> tasks can be executed serially
 */
public abstract class DispatchQueue {
	/**
	 * Add <code>{@link Runnable}</code> task to the queue
	 */
	public abstract void dispatchAsync(Runnable runnable);

	/**
	 * A global dispatch queue associated with main thread
	 */
	public static DispatchQueue mainQueue() {
		return Holder.INSTANCE;
	}

	/**
	 * Thread safe singleton trick
	 */
	private static class Holder {
		private static final DispatchQueue INSTANCE = createMainQueue();

		private static DispatchQueue createMainQueue() {
			try {
				// this call will fail when running a unit test
				// we would allow that and make test responsible for setting the implementation
				return new HandlerDispatchQueue(Looper.getMainLooper());
			} catch (Exception e) {
				return null;
			}
		}
	}
}