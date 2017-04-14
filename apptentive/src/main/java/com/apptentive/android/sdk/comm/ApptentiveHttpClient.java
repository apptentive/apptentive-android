package com.apptentive.android.sdk.comm;

import com.apptentive.android.sdk.debug.Assert;
import com.apptentive.android.sdk.model.AppReleasePayload;
import com.apptentive.android.sdk.model.ConversationTokenRequest;
import com.apptentive.android.sdk.model.DevicePayload;
import com.apptentive.android.sdk.model.EventPayload;
import com.apptentive.android.sdk.model.Payload;
import com.apptentive.android.sdk.model.PersonPayload;
import com.apptentive.android.sdk.model.SdkAndAppReleasePayload;
import com.apptentive.android.sdk.model.SdkPayload;
import com.apptentive.android.sdk.model.SurveyResponsePayload;
import com.apptentive.android.sdk.network.HttpJsonRequest;
import com.apptentive.android.sdk.network.HttpRequest;
import com.apptentive.android.sdk.network.HttpRequestManager;
import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.storage.PayloadRequestSender;
import com.apptentive.android.sdk.util.Constants;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

/**
 * Class responsible for all client-server network communications using asynchronous HTTP requests
 */
public class ApptentiveHttpClient implements PayloadRequestSender {
	public static final String API_VERSION = "7";

	private static final String USER_AGENT_STRING = "Apptentive/%s (Android)"; // Format with SDK version string.

	public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 45000;
	public static final int DEFAULT_HTTP_SOCKET_TIMEOUT = 45000;

	// Active API
	private static final String ENDPOINT_CONVERSATION = "/conversation";
	private static final String ENDPOINT_EVENTS = "/events";
	private static final String ENDPOINT_DEVICES = "/devices";
	private static final String ENDPOINT_PEOPLE = "/people";
	private static final String ENDPOINT_SURVEYS_POST = "/surveys/%s/respond";

	private final String apiKey;
	private final String serverURL;
	private final String userAgentString;
	private final HttpRequestManager httpRequestManager;

	public ApptentiveHttpClient(String apiKey, String serverURL) {
		if (isEmpty(apiKey)) {
			throw new IllegalArgumentException("Illegal API key: '" + apiKey + "'");
		}

		if (isEmpty(serverURL)) {
			throw new IllegalArgumentException("Illegal server URL: '" + serverURL + "'");
		}

		this.httpRequestManager = new HttpRequestManager();
		this.apiKey = apiKey;
		this.serverURL = serverURL;
		this.userAgentString = String.format(USER_AGENT_STRING, Constants.APPTENTIVE_SDK_VERSION);
	}

	//region API Requests

	public HttpJsonRequest getConversationToken(ConversationTokenRequest conversationTokenRequest, HttpRequest.Listener<HttpJsonRequest> listener) {
		HttpJsonRequest request = createJsonRequest(apiKey, ENDPOINT_CONVERSATION, conversationTokenRequest, HttpRequestMethod.POST);
		request.addListener(listener);
		httpRequestManager.startRequest(request);
		return request;
	}

	/**
	 * Returns the first request with a given tag or <code>null</code> is not found
	 */
	public HttpRequest findRequest(String tag) {
		return httpRequestManager.findRequest(tag);
	}

	//endregion

	//region PayloadRequestSender

	@Override
	public HttpRequest sendPayload(Payload payload, HttpRequest.Listener<HttpRequest> listener) {
		if (payload == null) {
			throw new IllegalArgumentException("Payload is null");
		}

		HttpRequest request = createRequest(payload);
		request.addListener(listener);
		httpRequestManager.startRequest(request);
		return request;
	}

	private HttpRequest createRequest(Payload payload) {
		final String token = payload.getToken();
		final String endPoint = payload.getHttpEndPoint();
		final HttpRequestMethod requestMethod = payload.getHttpRequestMethod();

		switch (payload.getHttpRequestContentType()) {
			case "application/json": {
				return createJsonRequest(token, endPoint, payload, requestMethod);
			}
		}

		throw new IllegalArgumentException("Unexpected content type: " + payload.getHttpRequestContentType());
	}

	//endregion

	//region Helpers

	private HttpJsonRequest createJsonRequest(String oauthToken, String endpoint, JSONObject jsonObject, HttpRequestMethod method) {
		Assert.assertNotNull(oauthToken);
		Assert.assertNotNull(endpoint);

		String url = createEndpointURL(endpoint);
		HttpJsonRequest request = new HttpJsonRequest(url, jsonObject);
		setupRequestDefaults(request, oauthToken);
		request.setMethod(method);
		request.setRequestProperty("Content-Type", "application/json");
		return request;
	}

	private void setupRequestDefaults(HttpRequest request, String oauthToken) {
		request.setRequestProperty("User-Agent", userAgentString);
		request.setRequestProperty("Connection", "Keep-Alive");
		request.setRequestProperty("Authorization", "OAuth " + oauthToken);
		request.setRequestProperty("Accept-Encoding", "gzip");
		request.setRequestProperty("Accept", "application/json");
		request.setRequestProperty("X-API-Version", API_VERSION);
		request.setConnectTimeout(DEFAULT_HTTP_CONNECT_TIMEOUT);
		request.setReadTimeout(DEFAULT_HTTP_SOCKET_TIMEOUT);
	}

	private String createEndpointURL(String uri) {
		return serverURL + uri;
	}

	//endregion
}
