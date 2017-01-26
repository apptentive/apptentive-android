//
//  DispatchQueue.java
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