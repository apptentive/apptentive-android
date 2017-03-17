package com.apptentive.android.sdk.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
		statusLookup.put(400, "Bad Request");
		statusLookup.put(500, "Internal Server Error");
	}

	boolean throwsExceptionOnConnect;
	boolean throwsExceptionOnDisconnect;

	private ResponseHandler responseHandler = new DefaultResponseHandler(200, "", ""); // HTTP OK by default
	private int lastResponseCode; // remember the last returned HTTP response code to properly resolve response message

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
		return new ByteArrayInputStream(responseHandler.getResponseData().getBytes("UTF-8"));
	}

	@Override
	public InputStream getErrorStream() {
		try {
			return new ByteArrayInputStream(responseHandler.getErrorData().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return new ByteArrayOutputStream();
	}

	@Override
	public int getResponseCode() throws IOException {
		if (throwsExceptionOnConnect) {
			throw new IOException("Connection error");
		}
		lastResponseCode = responseHandler.getResponseCode();
		return lastResponseCode;
	}

	@Override
	public String getResponseMessage() throws IOException {
		return statusLookup.get(lastResponseCode);
	}

	@Override
	public void setRequestMethod(String method) throws ProtocolException {
	}

	public void setMockResponseCode(int mockResponseCode) {
		((DefaultResponseHandler) responseHandler).setResponseCode(mockResponseCode);
	}

	public void setMockResponseData(String responseData) {
		((DefaultResponseHandler) responseHandler).setResponseData(responseData);
	}

	public void setMockResponseHandler(ResponseHandler handler) {
		responseHandler = handler;
	}

	public interface ResponseHandler {
		int getResponseCode();
		String getResponseData();
		String getErrorData();
	}

	public static class AbstractResponseHandler implements ResponseHandler {
		@Override
		public int getResponseCode() {
			return 200;
		}

		@Override
		public String getResponseData() {
			return "";
		}

		@Override
		public String getErrorData() {
			return "";
		}
	}

	private static class DefaultResponseHandler implements ResponseHandler {
		private int responseCode;
		private String responseData;
		private String errorData;

		public DefaultResponseHandler(int responseCode, String responseData, String errorData) {
			this.responseCode = responseCode;
			this.responseData = responseData;
			this.errorData = errorData;
		}

		public DefaultResponseHandler setResponseCode(int responseCode) {
			this.responseCode = responseCode;
			return this;
		}

		@Override
		public int getResponseCode() {
			return responseCode;
		}

		public DefaultResponseHandler setResponseData(String responseData) {
			this.responseData = responseData;
			return this;
		}

		@Override
		public String getResponseData() {
			return responseData;
		}

		public DefaultResponseHandler setErrorData(String errorData) {
			this.errorData = errorData;
			return this;
		}

		@Override
		public String getErrorData() {
			return errorData;
		}
	}
}
