/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

/**
 * Tool class for creating handlers that hold only a weak reference to their
 * callback. This avoids leaking instances of the callback when the handler
 * is retained by messages in the queue.
 */
public class WeakReferenceHandler {

	private WeakReferenceHandler() {
		// do not instantiate
	}

	/**
	 * Returns a Handler with a WeakReference to the given callback.
	 *
	 * @param callback message handler callback
	 * @return handler with weak reference to callback
	 */
	public static Handler create(Callback callback) {
		return new Handler(new WeakReferenceHandlerCallback(callback));
	}

	/**
	 * Callback that wraps a given callback in a WeakReference. Holders of
	 * this callback will not prevent the wrapped callback instance from
	 * being garbage collected.
	 */
	private static class WeakReferenceHandlerCallback implements Callback {

		private final WeakReference<Callback> callbackRef;

		public WeakReferenceHandlerCallback(Callback callback) {
			this.callbackRef = new WeakReference<Callback>(callback);
		}

		public boolean handleMessage(Message msg) {
			Callback callback = callbackRef.get();
			return (callback != null) ? callback.handleMessage(msg) : true;
		}
	}
}
