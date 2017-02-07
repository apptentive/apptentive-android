package com.apptentive.android.sdk.network;

import org.json.JSONObject;

/**
 * Class representing HTTP request with Json POST body
 */
public class HttpJsonRequest extends HttpRequest {
	private final JSONObject jsonObject;

	public HttpJsonRequest(String urlString, JSONObject jsonObject) {
		super(urlString);

		if (jsonObject == null) {
			throw new IllegalArgumentException("Json object is null");
		}
		this.jsonObject = jsonObject;
	}
}
