package com.apptentive.android.sdk.network;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

class MockHttpJsonRequest extends HttpJsonRequest {

	private final MockHttpURLConnection connection;

	MockHttpJsonRequest(String name, JSONObject requestObject) {
		super("https://abc.com", requestObject);
		connection = new MockHttpURLConnection();
		connection.setMockResponseCode(200);
		setName(name);
		setMethod(HttpRequestMethod.POST);
	}

	@Override
	protected HttpURLConnection openConnection(URL url) throws IOException {
		return connection;
	}

	@Override
	public String toString() {
		return getName();
	}

	public MockHttpJsonRequest setMockResponseData(JSONObject responseData) {
		return setMockResponseData(responseData.toString());
	}

	public MockHttpJsonRequest setMockResponseData(String responseData) {
		connection.responseData = responseData;
		return this;
	}
}
