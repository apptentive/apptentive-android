//
//  HandlerDispatchQueue.java
//
//  Lunar Unity Mobile Console
//  https://github.com/SpaceMadness/lunar-unity-console
//
//  Copyright 2017 Alex Lementuev, SpaceMadness.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

package com.apptentive.android.sdk.util.threading;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import static com.apptentive.android.sdk.debug.Assert.assertNotNull;

/**
 * Serial dispatch queue implementation based on {@link Handler}
 */
class SerialDispatchQueue extends DispatchQueue {
	private final Handler handler;
	private final HandlerThread handlerThread;

	/**
	 * Creates a private queue with specified <code>name</code>
	 */
	SerialDispatchQueue(String name) {
		super(name);
		handlerThread = new HandlerThread(name);
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper());
	}

	/**
	 * Creates a queue with specified <code>looper</code>
	 */
	SerialDispatchQueue(Looper looper, String name) {
		super(name);
		if (looper == null) {
			throw new NullPointerException("Looper is null");
		}
		handler = new Handler(looper);
		handlerThread = null; // non-private queue (no explicit thread)
	}

	@Override
	protected void dispatch(DispatchTask task, long delayMillis) {
		if (delayMillis > 0) {
			handler.postDelayed(task, delayMillis);
		} else {
			handler.post(task);
		}
	}

	@Override
	public void stop() {
		assertNotNull(handlerThread, "Attempted to stop a non-private queue '%s'", handler.getLooper().getThread());
		if (handlerThread != null) {
			handler.removeCallbacks(null);
			handlerThread.quit();
		}
	}

	@Override
	public boolean isCurrent() {
		return Looper.myLooper() == handler.getLooper();
	}
}