package com.apptentive.android.sdk.network;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.Map;

class MockHttpURLConnection extends HttpURLConnection {
	private static final Map<Integer, String> statusLookup;

	static {
		statusLookup = new HashMap<>();
		statusLookup.put(200, "OK");
		statusLookup.put(204, "No Content");
		statusLookup.put(500, "Internal Server Error");
	}

	boolean throwsExceptionOnConnect;
	boolean throwsExceptionOnDisconnect;
	int mockResponseCode = 200; // HTTP OK by default
	String mockResponseMessage = "OK";
	String responseData = "";
	String errorData = "";

	protected MockHttpURLConnection() {
		super(null);
	}

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

	public void setMockResponseCode(int mockResponseCode) {
		this.mockResponseCode = mockResponseCode;
		this.mockResponseMessage = statusLookup.get(mockResponseCode);
	}
}
