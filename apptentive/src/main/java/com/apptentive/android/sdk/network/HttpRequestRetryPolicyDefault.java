/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.network;

import java.util.Random;

public class HttpRequestRetryPolicyDefault implements HttpRequestRetryPolicy {
	public static final int RETRY_COUNT_INFINITE = -1;

	public static final long DEFAULT_RETRY_TIMEOUT_MILLIS = 5 * 1000;
	public static final int DEFAULT_RETRY_COUNT = 5;

	/**
	 * Maximum retry timeout for the exponential back-off
	 */
	private static final long MAX_RETRY_CAP = 10 * 60 * 1000L;

	/**
	 * How many times should request retry before giving up
	 */
	private int maxRetryCount = DEFAULT_RETRY_COUNT;

	/**
	 * How long should we wait before retrying again
	 */
	private long retryTimeoutMillis = DEFAULT_RETRY_TIMEOUT_MILLIS;

	private static final Random RANDOM = new Random();

	/**
	 * Returns <code>true</code> is request should be retried.
	 *
	 * @param responseCode - HTTP response code for the request
	 */
	@Override
	public boolean shouldRetryRequest(int responseCode, int retryAttempt) {
		if (responseCode >= 400 && responseCode < 500) {
			return false; // don't retry if request was rejected permanently
		}

		if (maxRetryCount == RETRY_COUNT_INFINITE) {
			return true; // keep retrying indefinitely
		}

		return retryAttempt <= maxRetryCount; // retry if we still can
	}

	/**
	 * Returns the delay in millis for the next retry
	 *
	 * @param retryAttempt - number of retries attempted already
	 */
	@Override
	public long getRetryTimeoutMillis(int retryAttempt) {
		long temp = Math.min(MAX_RETRY_CAP, (long) (retryTimeoutMillis * Math.pow(2.0, retryAttempt - 1)));
		return (long) ((temp / 2) * (1.0 + RANDOM.nextDouble()));
	}

	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	public void setRetryTimeoutMillis(long retryTimeoutMillis) {
		this.retryTimeoutMillis = retryTimeoutMillis;
	}
}
