package com.apptentive.android.sdk.comm;

import com.apptentive.android.sdk.model.ConversationTokenRequest;
import com.apptentive.android.sdk.network.HttpJsonRequest;
import com.apptentive.android.sdk.network.HttpRequest;
import com.apptentive.android.sdk.network.HttpRequestManager;
import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.util.Constants;

import org.json.JSONObject;

import static android.text.TextUtils.isEmpty;

/**
 * Class responsible for all client-server network communications using asynchronous HTTP requests
 */
public class ApptentiveHttpClient {
	public static final String API_VERSION = "7";

	private static final String USER_AGENT_STRING = "Apptentive/%s (Android)"; // Format with SDK version string.

	public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 45000;
	public static final int DEFAULT_HTTP_SOCKET_TIMEOUT = 45000;

	// Active API
	private static final String ENDPOINT_CONVERSATION = "/conversation";

	private final String oauthToken;
	private final String serverURL;
	private final String userAgentString;
	private final HttpRequestManager httpRequestManager;

	public ApptentiveHttpClient(String oauthToken, String serverURL) {
		if (isEmpty(oauthToken)) {
			throw new IllegalArgumentException("Illegal OAuth Token: '" + oauthToken + "'");
		}

		if (isEmpty(serverURL)) {
			throw new IllegalArgumentException("Illegal server URL: '" + serverURL + "'");
		}

		this.httpRequestManager = new HttpRequestManager();
		this.oauthToken = oauthToken;
		this.serverURL = serverURL;
		this.userAgentString = String.format(USER_AGENT_STRING, Constants.APPTENTIVE_SDK_VERSION);
	}

	//region API Requests

	public HttpJsonRequest getConversationToken(ConversationTokenRequest conversationTokenRequest, HttpRequest.Listener<HttpJsonRequest> listener) {
		return startJsonRequest(ENDPOINT_CONVERSATION, conversationTokenRequest, listener);
	}

	//endregion

	//region Helpers

	private HttpJsonRequest startJsonRequest(String endpoint, JSONObject jsonObject, HttpRequest.Listener<HttpJsonRequest> listener) {
		String url = createEndpointURL(endpoint);
		HttpJsonRequest request = new HttpJsonRequest(url, jsonObject);
		setupRequestDefaults(request);
		request.setMethod(HttpRequestMethod.POST);
		request.setRequestProperty("Content-Type", "application/json");
		request.setListener(listener);
		httpRequestManager.startRequest(request);
		return request;
	}

	private void setupRequestDefaults(HttpRequest request) {
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
