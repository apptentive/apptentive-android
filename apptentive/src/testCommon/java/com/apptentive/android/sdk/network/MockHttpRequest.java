/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.network;

import com.apptentive.android.sdk.network.MockHttpURLConnection.ResponseHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MockHttpRequest extends HttpRequest {

	private final MockHttpURLConnection connection;

	public MockHttpRequest(String name) {
		super("https://abc.com");
		connection = new MockHttpURLConnection();
		connection.setMockResponseCode(200);
		setName(name);
		setRetryPolicy(new HttpRequestRetryPolicyDefault() {
			@Override
			public boolean shouldRetryRequest(int responseCode, int retryAttempt) {
				return false; // do not retry by default
			}
		});
	}

	@Override
	protected HttpURLConnection openConnection(URL url) throws IOException {
		return connection;
	}

	public MockHttpRequest setThrowsExceptionOnConnect(boolean throwsExceptionOnConnect) {
		connection.throwsExceptionOnConnect = throwsExceptionOnConnect;
		return this;
	}

	public MockHttpRequest setThrowsExceptionOnDisconnect(boolean throwsExceptionOnDisconnect) {
		connection.throwsExceptionOnDisconnect = throwsExceptionOnDisconnect;
		return this;
	}

	public MockHttpRequest setMockResponseHandler(ResponseHandler handler) {
		connection.setMockResponseHandler(handler);
		return this;
	}

	public MockHttpRequest setMockResponseCode(int mockResponseCode) {
		connection.setMockResponseCode(mockResponseCode);
		return this;
	}

	public MockHttpRequest setResponseData(String responseData) {
		connection.setMockResponseData(responseData);
		return this;
	}

	@Override
	public String toString() {
		return getName();
	}
}
