/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.network;

public class HttpRequestRetryPolicy {
	public static final int RETRY_INDEFINITELY = -1;
	public static final long DEFAULT_RETRY_TIMEOUT_MILLIS = 5 * 1000;

	/**
	 * How many times should request retry before giving up
	 */
	private int maxRetryCount = RETRY_INDEFINITELY;

	/**
	 * How long should we wait before retrying again
	 */
	private long retryTimeoutMillis = DEFAULT_RETRY_TIMEOUT_MILLIS;

	protected boolean shouldRetryRequest(int responseCode) {
		return false; // TODO: decide based on response code
	}

	public int getMaxRetryCount() {
		return maxRetryCount;
	}

	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	public long getRetryTimeoutMillis() {
		return retryTimeoutMillis;
	}

	public void setRetryTimeoutMillis(long retryTimeoutMillis) {
		this.retryTimeoutMillis = retryTimeoutMillis;
	}
}
