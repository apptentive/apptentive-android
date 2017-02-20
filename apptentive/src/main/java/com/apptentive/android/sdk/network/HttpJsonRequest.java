package com.apptentive.android.sdk.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Class representing HTTP request with Json POST body
 */
public class HttpJsonRequest extends HttpRequest {
	private final JSONObject requestObject;
	private JSONObject responseObject;

	public HttpJsonRequest(String urlString, JSONObject requestObject) {
		super(urlString);

		if (requestObject == null) {
			throw new IllegalArgumentException("Json object is null");
		}
		this.requestObject = requestObject;
	}

	@Override
	protected byte[] createRequestData() throws IOException {
		String json = requestObject.toString();
		return json.getBytes("UTF-8");
	}

	@Override
	protected void handleResponse(String response) throws IOException {
		try {
			responseObject = new JSONObject(response);
		} catch (JSONException e) {
			throw new IOException(e);
		}
	}

	public JSONObject getResponseObject() {
		return responseObject;
	}
}
