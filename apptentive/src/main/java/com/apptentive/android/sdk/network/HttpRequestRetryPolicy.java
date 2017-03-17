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

	/**
	 * Returns <code>true</code> is request should be retried.
	 *
	 * @param responseCode - HTTP response code for the request
	 */
	protected boolean shouldRetryRequest(int responseCode) {
		return false; // TODO: decide based on response code
	}

	/**
	 * Returns the delay in millis for the next retry
	 *
	 * @param retryCount - number of retries attempted already
	 */
	protected long getRetryTimeoutMillis(int retryCount) {
		return retryTimeoutMillis;
	}

	public int getMaxRetryCount() {
		return maxRetryCount;
	}

	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	public void setRetryTimeoutMillis(long retryTimeoutMillis) {
		this.retryTimeoutMillis = retryTimeoutMillis;
	}
}
