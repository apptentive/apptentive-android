/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.threading;

/**
 * Describes the type of dispatch queue
 */
public enum DispatchQueueType {
	/**
	 * Tasks are executed one after another on the same thread (no more than one task is executing at the same time
	 */
	Serial,
	/**
	 * Task are executed in parallel on multiple threads (two or more tasks can be executed at the same time
	 */
	Concurrent
}
