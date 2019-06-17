/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.comm;

import com.apptentive.android.sdk.model.ConversationTokenRequest;
import com.apptentive.android.sdk.model.PayloadData;
import com.apptentive.android.sdk.network.HttpJsonRequest;
import com.apptentive.android.sdk.network.HttpRequest;
import com.apptentive.android.sdk.network.HttpRequestManager;
import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.network.RawHttpRequest;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.AppReleaseManager;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.DevicePayloadDiff;
import com.apptentive.android.sdk.storage.PayloadRequestSender;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.storage.SdkManager;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static com.apptentive.android.sdk.debug.Assert.notNull;

/**
 * Class responsible for all client-server network communications using asynchronous HTTP requests
 */
public class ApptentiveHttpClient implements PayloadRequestSender {

	public static final String USER_AGENT_STRING = "Apptentive/%s (Android)"; // Format with SDK version string.

	private static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 45000;
	private static final int DEFAULT_HTTP_SOCKET_TIMEOUT = 45000;

	// Active API
	private static final String ENDPOINT_CONVERSATION = "/conversation";
	private static final String ENDPOINT_CONFIGURATION = "/conversations/%s/configuration";
	private static final String ENDPOINT_LEGACY_CONVERSATION = "/conversation/token";
	private static final String ENDPOINT_LOG_IN_TO_EXISTING_CONVERSATION = "/conversations/%s/session";
	private static final String ENDPOINT_LOG_IN_TO_NEW_CONVERSATION = "/conversations";
	private static final String ENDPOINT_INTERACTIONS = "/conversations/%s/interactions";
	private static final String ENDPOINT_MESSAGES = "/conversations/%s/messages?count=%s&starts_after=%s&before_id=%s";

	private final String apptentiveKey;
	private final String apptentiveSignature;
	private final String serverURL;
	private final String userAgentString;

	public ApptentiveHttpClient(String apptentiveKey, String apptentiveSignature, String serverURL) {
		if (StringUtils.isNullOrEmpty(apptentiveKey)) {
			throw new IllegalArgumentException("Illegal Apptentive Key: '" + apptentiveKey + "'");
		}

		if (StringUtils.isNullOrEmpty(apptentiveSignature)) {
			throw new IllegalArgumentException("Illegal Apptentive Signature: '" + apptentiveSignature + "'");
		}

		if (StringUtils.isNullOrEmpty(serverURL)) {
			throw new IllegalArgumentException("Illegal server URL: '" + serverURL + "'");
		}

		this.apptentiveKey = apptentiveKey;
		this.apptentiveSignature = apptentiveSignature;
		this.serverURL = serverURL;
		this.userAgentString = String.format(USER_AGENT_STRING, Constants.getApptentiveSdkVersion());
	}

	//region API Requests

	public HttpJsonRequest createConversationTokenRequest(ConversationTokenRequest conversationTokenRequest, HttpRequest.Listener<HttpJsonRequest> listener) {
		HttpJsonRequest request = createJsonRequest(ENDPOINT_CONVERSATION, conversationTokenRequest, HttpRequestMethod.POST);
		request.addListener(listener);
		return request;
	}

	public HttpJsonRequest createLegacyConversationIdRequest(String conversationToken, HttpRequest.Listener<HttpJsonRequest> listener) {
		if (StringUtils.isNullOrEmpty(conversationToken)) {
			throw new IllegalArgumentException("Conversation token is null or empty");
		}

		HttpJsonRequest request = createJsonRequest(ENDPOINT_LEGACY_CONVERSATION, new JSONObject(), HttpRequestMethod.GET);
		request.setRequestProperty("Authorization", "OAuth " + conversationToken);
		request.addListener(listener);
		return request;
	}

	public HttpJsonRequest createFetchInteractionsRequest(String conversationToken, String conversationId, HttpRequest.Listener<HttpJsonRequest> listener) {
		if (StringUtils.isNullOrEmpty(conversationToken)) {
			throw new IllegalArgumentException("Conversation token is null or empty");
		}

		if (StringUtils.isNullOrEmpty(conversationId)) {
			throw new IllegalArgumentException("Conversation id is null or empty");
		}

		final String endPoint = StringUtils.format(ENDPOINT_INTERACTIONS, conversationId);
		HttpJsonRequest request = createJsonRequest(endPoint, new JSONObject(), HttpRequestMethod.GET);
		request.setRequestProperty("Authorization", "Bearer " + conversationToken);
		request.addListener(listener);
		return request;
	}

	public HttpJsonRequest createFetchMessagesRequest(String conversationToken, String conversationId, String afterId, String beforeId, Integer count, HttpRequest.Listener<HttpJsonRequest> listener) {
		if (StringUtils.isNullOrEmpty(conversationToken)) {
			throw new IllegalArgumentException("Conversation token is null or empty");
		}

		if (StringUtils.isNullOrEmpty(conversationId)) {
			throw new IllegalArgumentException("Conversation id is null or empty");
		}

		final String endPoint = String.format(ENDPOINT_MESSAGES, conversationId, count == null ? "" : count.toString(), afterId == null ? "" : afterId, beforeId == null ? "" : beforeId);
		HttpJsonRequest request = createJsonRequest(endPoint, new JSONObject(), HttpRequestMethod.GET);
		request.setRequestProperty("Authorization", "Bearer " + conversationToken);
		request.addListener(listener);
		return request;
	}

	public HttpJsonRequest createLoginRequest(String conversationId, String token, HttpRequest.Listener<HttpJsonRequest> listener) {
		if (token == null) {
			throw new IllegalArgumentException("Token is null");
		}

		JSONObject json = new JSONObject();
		try {
			json.put("token", token);
		} catch (JSONException e) {
			// Can't happen
		}
		String endPoint;
		if (conversationId == null) {
			endPoint = ENDPOINT_LOG_IN_TO_NEW_CONVERSATION;

		} else {
			endPoint = StringUtils.format(ENDPOINT_LOG_IN_TO_EXISTING_CONVERSATION, conversationId);
		}
		HttpJsonRequest request = createJsonRequest(endPoint, json, HttpRequestMethod.POST);
		request.addListener(listener);
		return request;
	}

	public HttpJsonRequest createAppConfigurationRequest(String conversationId, String token, HttpRequest.Listener<HttpJsonRequest> listener) {
		if (StringUtils.isNullOrEmpty(conversationId)) {
			throw new IllegalArgumentException("Conversation id is null or empty");
		}

		if (StringUtils.isNullOrEmpty(token)) {
			throw new IllegalArgumentException("Conversation token is null or empty");
		}

		String endPoint = StringUtils.format(ENDPOINT_CONFIGURATION, conversationId);
		HttpJsonRequest request = createJsonRequest(endPoint, new JSONObject(), HttpRequestMethod.GET);
		request.setRequestProperty("Authorization", "Bearer " + token);
		request.addListener(listener);
		return request;
	}

	public HttpJsonRequest createFirstLoginRequest(String token, AppRelease appRelease, Sdk sdk, Device device, HttpRequest.Listener<HttpJsonRequest> listener) {
		if (token == null) {
			throw new IllegalArgumentException("Token is null");
		}

		ConversationTokenRequest conversationTokenRequest = new ConversationTokenRequest();
		conversationTokenRequest.setSdkAndAppRelease(SdkManager.getPayload(sdk), AppReleaseManager.getPayload(appRelease));
		conversationTokenRequest.setDevice(DevicePayloadDiff.getDiffPayload(null, device));

		try {
			conversationTokenRequest.put("token", token);
		} catch (JSONException e) {
			// Can't happen
		}

		HttpJsonRequest request = createJsonRequest(ENDPOINT_LOG_IN_TO_NEW_CONVERSATION, conversationTokenRequest, HttpRequestMethod.POST);
		request.addListener(listener);
		return request;
	}

	/**
	 * Returns the first request with a given tag or <code>null</code> is not found
	 */
	public HttpRequest findRequest(String tag) {
		return HttpRequestManager.sharedManager().findRequest(tag);
	}

	//endregion

	//region PayloadRequestSender

	@Override
	public HttpRequest createPayloadSendRequest(PayloadData payload, HttpRequest.Listener<HttpRequest> listener) {
		if (payload == null) {
			throw new IllegalArgumentException("Payload is null");
		}

		HttpRequest request = createPayloadRequest(payload);
		request.addListener(listener);
		return request;
	}

	private HttpRequest createPayloadRequest(PayloadData payload) {
		final String authToken = payload.getAuthToken();
		final String httpPath = notNull(payload.getHttpRequestPath());
		final HttpRequestMethod requestMethod = notNull(payload.getHttpRequestMethod());
		final String contentType = notNull(payload.getContentType());

		HttpRequest request = createRawRequest(httpPath, payload.getData(), requestMethod, contentType);

		// Encrypted requests don't use an Auth token on the request. It's stored in the encrypted body.
		if (!StringUtils.isNullOrEmpty(authToken)) {
			request.setRequestProperty("Authorization", "Bearer " + authToken);
		}

		if (payload.isAuthenticated()) {
			request.setRequestProperty("APPTENTIVE-ENCRYPTED", Boolean.TRUE);
		}

		return request;
	}

	//endregion

	//region Helpers

	private HttpJsonRequest createJsonRequest(String endpoint, JSONObject json, HttpRequestMethod method) {
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
		setupRequestDefaults(request);
		request.setMethod(method);
		request.setRequestProperty("Content-Type", "application/json");
		return request;
	}

	private RawHttpRequest createRawRequest(String endpoint, byte[] data, HttpRequestMethod method, String contentType) {
		if (endpoint == null) {
			throw new IllegalArgumentException("Endpoint is null");
		}
		if (data == null) {
			throw new IllegalArgumentException("Payload is null");
		}
		if (method == null) {
			throw new IllegalArgumentException("Method is null");
		}
		if (contentType == null) {
			throw new IllegalArgumentException("ContentType is null");
		}

		String url = createEndpointURL(endpoint);
		RawHttpRequest request = new RawHttpRequest(url, data);
		setupRequestDefaults(request);
		request.setMethod(method);
		request.setRequestProperty("Content-Type", contentType);
		return request;
	}

	private void setupRequestDefaults(HttpRequest request) {
		request.setRequestManager(HttpRequestManager.sharedManager());
		request.setRequestProperty("User-Agent", userAgentString);
		request.setRequestProperty("Connection", "Keep-Alive");
		request.setRequestProperty("Accept-Encoding", "gzip");
		request.setRequestProperty("Accept", "application/json");
		request.setRequestProperty("APPTENTIVE-KEY", apptentiveKey);
		request.setRequestProperty("APPTENTIVE-SIGNATURE", apptentiveSignature);
		request.setRequestProperty("X-API-Version", String.valueOf(Constants.API_VERSION));
		request.setConnectTimeout(DEFAULT_HTTP_CONNECT_TIMEOUT);
		request.setReadTimeout(DEFAULT_HTTP_SOCKET_TIMEOUT);
	}

	private String createEndpointURL(String uri) {
		return serverURL + uri;
	}

	//endregion
}
