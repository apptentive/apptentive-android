package com.apptentive.android.sdk.network;

import android.util.SparseArray;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

class MockHttpRequest extends HttpRequest {

	private static final Map<Integer, String> statusLookup;

	static {
		statusLookup = new HashMap<>();
		statusLookup.put(200, "OK");
		statusLookup.put(204, "No Content");
		statusLookup.put(500, "Internal Server Error");
	}

	private boolean throwsExceptionOnConnect;
	private boolean throwsExceptionOnDisconnect;
	private int mockResponseCode = 200; // HTTP OK by default
	private String mockResponseMessage = "OK";
	private String responseData = "";
	private String errorData = "";

	MockHttpRequest(String name) {
		super("https://abc.com");
		setName(name);
	}

	@Override
	protected HttpURLConnection openConnection(URL url) throws IOException {
		return new HttpURLConnection(url) {

			@Override
			public boolean usingProxy() {
				return false;
			}

			@Override
			public void connect() throws IOException {
				connected = true;
			}

			@Override
			public void disconnect() {
				connected = false;
				if (throwsExceptionOnDisconnect) {
					throw new RuntimeException("Disconnection error");
				}
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(responseData.getBytes("UTF-8"));
			}

			@Override
			public InputStream getErrorStream() {
				try {
					return new ByteArrayInputStream(errorData.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new AssertionError(e);
				}
			}

			@Override
			public int getResponseCode() throws IOException {
				if (throwsExceptionOnConnect) {
					throw new IOException("Connection error");
				}
				return mockResponseCode;
			}

			@Override
			public String getResponseMessage() throws IOException {
				return mockResponseMessage;
			}

			@Override
			public void setRequestMethod(String method) throws ProtocolException {
			}
		};
	}

	public MockHttpRequest setThrowsExceptionOnConnect(boolean throwsExceptionOnConnect) {
		this.throwsExceptionOnConnect = throwsExceptionOnConnect;
		return this;
	}

	public MockHttpRequest setThrowsExceptionOnDisconnect(boolean throwsExceptionOnDisconnect) {
		this.throwsExceptionOnDisconnect = throwsExceptionOnDisconnect;
		return this;
	}

	public MockHttpRequest setMockResponseCode(int mockResponseCode) {
		this.mockResponseCode = mockResponseCode;
		this.mockResponseMessage = statusLookup.get(mockResponseCode);
		return this;
	}

	public MockHttpRequest setResponseData(String responseData) {
		this.responseData = responseData;
		return this;
	}

	public MockHttpRequest setErrorData(String errorData) {
		this.errorData = errorData;
		return this;
	}

	@Override
	public String toString() {
		return getName();
	}
}
