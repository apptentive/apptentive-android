package com.apptentive.android.sdk.network;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

class MockHttpRequest extends HttpRequest {

	private final MockHttpURLConnection connection;

	MockHttpRequest(String name) {
		super("https://abc.com");
		connection = new MockHttpURLConnection();
		connection.setMockResponseCode(200);
		setName(name);
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

	public MockHttpRequest setMockResponseCode(int mockResponseCode) {
		connection.setMockResponseCode(mockResponseCode);
		return this;
	}

	public MockHttpRequest setResponseData(String responseData) {
		connection.responseData = responseData;
		return this;
	}

	@Override
	public String toString() {
		return getName();
	}
}
