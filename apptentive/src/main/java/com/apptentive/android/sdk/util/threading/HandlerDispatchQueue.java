/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.threading;

import android.os.Handler;
import android.os.Looper;

public class HandlerDispatchQueue extends DispatchQueue {
	private final Handler handler;

	public HandlerDispatchQueue(Looper looper) {
		if (looper == null) {
			throw new NullPointerException("Looper is null");
		}
		handler = new Handler(looper);
	}

	@Override
	public void dispatchAsync(Runnable runnable) {
		handler.post(runnable);
	}
}