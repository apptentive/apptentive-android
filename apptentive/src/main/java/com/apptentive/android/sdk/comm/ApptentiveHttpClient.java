package com.apptentive.android.sdk.comm;

import com.apptentive.android.sdk.model.ConversationTokenRequest;
import com.apptentive.android.sdk.model.MultipartPayload;
import com.apptentive.android.sdk.model.PayloadData;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.network.HttpJsonMultipartRequest;
import com.apptentive.android.sdk.network.HttpJsonRequest;
import com.apptentive.android.sdk.network.HttpRequest;
import com.apptentive.android.sdk.network.HttpRequestManager;
import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.network.RawHttpRequest;
import com.apptentive.android.sdk.storage.PayloadRequestSender;
import com.apptentive.android.sdk.util.Constants;

import org.json.JSONObject;

import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * Class responsible for all client-server network communications using asynchronous HTTP requests
 */
public class ApptentiveHttpClient implements PayloadRequestSender {
	private static final String API_VERSION = "7";

	private static final String USER_AGENT_STRING = "Apptentive/%s (Android)"; // Format with SDK version string.

	private static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 45000;
	private static final int DEFAULT_HTTP_SOCKET_TIMEOUT = 45000;

	// Active API
	private static final String ENDPOINT_CONVERSATION = "/conversation";

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
	public HttpRequest sendPayload(PayloadData payload, HttpRequest.Listener<HttpRequest> listener) {
		if (payload == null) {
			throw new IllegalArgumentException("Payload is null");
		}

		HttpRequest request = createPayloadRequest(payload);
		request.addListener(listener);
		httpRequestManager.startRequest(request);
		return request;
	}

	private HttpRequest createPayloadRequest(PayloadData payload) {
		final String token = payload.getAuthToken();
		final String httpPath = payload.getHttpRequestPath();
		final HttpRequestMethod requestMethod = payload.getHttpRequestMethod();

		// TODO: figure out a better solution
		if (payload instanceof MultipartPayload) {
			final List<StoredFile> associatedFiles = ((MultipartPayload) payload).getAssociatedFiles();
			return createMultipartRequest(token, httpPath, payload.getData(), associatedFiles, requestMethod);
		}

		return createRawRequest(token, httpPath, payload.getData(), requestMethod);
	}

	//endregion

	//region Helpers

	private HttpJsonRequest createJsonRequest(String oauthToken, String endpoint, JSONObject json, HttpRequestMethod method) {
		if (oauthToken == null) {
			throw new IllegalArgumentException("OAuth token is null");
		}
		if (endpoint == null) {
			throw new IllegalArgumentException("Endpoint is null");
		}
		if (json == null) {
			throw new IllegalArgumentException("Json is null");
		}
		if (method == null) {
			throw new IllegalArgumentException("Method is null");
		}

		String url = createEndpointURL(endpoint);
		HttpJsonRequest request = new HttpJsonRequest(url, json);
		setupRequestDefaults(request, oauthToken);
		request.setMethod(method);
		request.setRequestProperty("Content-Type", "application/json");
		return request;
	}

	private RawHttpRequest createRawRequest(String oauthToken, String endpoint, byte[] data, HttpRequestMethod method) {
		if (oauthToken == null) {
			throw new IllegalArgumentException("OAuth token is null");
		}
		if (endpoint == null) {
			throw new IllegalArgumentException("Endpoint is null");
		}
		if (data == null) {
			throw new IllegalArgumentException("Payload is null");
		}
		if (method == null) {
			throw new IllegalArgumentException("Method is null");
		}

		String url = createEndpointURL(endpoint);
		RawHttpRequest request = new RawHttpRequest(url, data);
		setupRequestDefaults(request, oauthToken);
		request.setMethod(method);
		request.setRequestProperty("Content-Type", "application/json");
		return request;
	}

	private HttpJsonMultipartRequest createMultipartRequest(String oauthToken, String endpoint, byte[] data, List<StoredFile> files, HttpRequestMethod method) {
		if (oauthToken == null) {
			throw new IllegalArgumentException("OAuth token is null");
		}
		if (endpoint == null) {
			throw new IllegalArgumentException("Endpoint is null");
		}
		if (data == null) {
			throw new IllegalArgumentException("Data is null");
		}
		if (method == null) {
			throw new IllegalArgumentException("Method is null");
		}

		String url = createEndpointURL(endpoint);
		HttpJsonMultipartRequest request = new HttpJsonMultipartRequest(url, data, files);
		setupRequestDefaults(request, oauthToken);
		request.setMethod(method);
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
