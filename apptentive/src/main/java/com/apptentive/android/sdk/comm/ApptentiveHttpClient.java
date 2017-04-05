package com.apptentive.android.sdk.comm;

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

	private final String oauthToken;
	private final String serverURL;
	private final String userAgentString;
	private final HttpRequestManager httpRequestManager;

	private final Map<Class<? extends Payload>, PayloadRequestFactory> payloadRequestFactoryLookup;

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
		this.payloadRequestFactoryLookup = createPayloadRequestFactoryLookup();
	}

	//region API Requests

	public HttpJsonRequest getConversationToken(ConversationTokenRequest conversationTokenRequest, HttpRequest.Listener<HttpJsonRequest> listener) {
		return startJsonRequest(ENDPOINT_CONVERSATION, conversationTokenRequest, HttpRequestMethod.POST, listener);
	}

	//endregion

	//region PayloadRequestSender

	@Override
	public HttpRequest sendPayload(Payload payload, HttpRequest.Listener<HttpJsonRequest> listener) {
		if (payload == null) {
			throw new IllegalArgumentException("Payload is null");
		}

		final PayloadRequestFactory requestFactory = payloadRequestFactoryLookup.get(payload.getClass());
		if (requestFactory == null) {
			throw new IllegalArgumentException("Unexpected payload type: " + payload.getClass());
		}

		HttpRequest request = requestFactory.createRequest(payload);
		request.setListener(listener);
		httpRequestManager.startRequest(request);
		return request;
	}

	//endregion

	//region Payload Request Factory

	private Map<Class<? extends Payload>, PayloadRequestFactory> createPayloadRequestFactoryLookup() {
		Map<Class<? extends Payload>, PayloadRequestFactory> lookup = new HashMap<>();

		// Event Payload
		lookup.put(EventPayload.class, new PayloadRequestFactory<EventPayload>() {
			@Override
			public HttpRequest createRequest(EventPayload payload) {
				return createJsonRequest(ENDPOINT_EVENTS, payload, HttpRequestMethod.POST);
			}
		});

		// Device Payload
		lookup.put(DevicePayload.class, new PayloadRequestFactory<DevicePayload>() {
			@Override
			public HttpRequest createRequest(DevicePayload payload) {
				return createJsonRequest(ENDPOINT_DEVICES, payload, HttpRequestMethod.PUT);
			}
		});

		// SDK Payload
		lookup.put(SdkPayload.class, new PayloadRequestFactory<SdkPayload>() {
			@Override
			public HttpRequest createRequest(SdkPayload payload) {
				return createJsonRequest(ENDPOINT_CONVERSATION, payload, HttpRequestMethod.PUT);
			}
		});

		// App Release Payload
		lookup.put(AppReleasePayload.class, new PayloadRequestFactory<AppReleasePayload>() {
			@Override
			public HttpRequest createRequest(AppReleasePayload payload) {
				return createJsonRequest(ENDPOINT_CONVERSATION, payload, HttpRequestMethod.PUT);
			}
		});

		// SDK and App Release Payload
		lookup.put(SdkAndAppReleasePayload.class, new PayloadRequestFactory<SdkAndAppReleasePayload>() {
			@Override
			public HttpRequest createRequest(SdkAndAppReleasePayload payload) {
				return createJsonRequest(ENDPOINT_CONVERSATION, payload, HttpRequestMethod.PUT);
			}
		});

		// Person Payload
		lookup.put(PersonPayload.class, new PayloadRequestFactory<PersonPayload>() {
			@Override
			public HttpRequest createRequest(PersonPayload payload) {
				return createJsonRequest(ENDPOINT_PEOPLE, payload, HttpRequestMethod.PUT);
			}
		});

		// Survey Payload
		lookup.put(SurveyResponsePayload.class, new PayloadRequestFactory<SurveyResponsePayload>() {
			@Override
			public HttpRequest createRequest(SurveyResponsePayload survey) {
				String endpoint = String.format(ENDPOINT_SURVEYS_POST, survey.getId());
				return createJsonRequest(endpoint, survey, HttpRequestMethod.POST);
			}
		});

		return lookup;
	}

	//endregion

	//region Helpers

	private HttpJsonRequest startJsonRequest(String endpoint, JSONObject jsonObject, HttpRequestMethod method, HttpRequest.Listener<HttpJsonRequest> listener) {
		HttpJsonRequest request = createJsonRequest(endpoint, jsonObject, method);
		request.setListener(listener);
		httpRequestManager.startRequest(request);
		return request;
	}

	private HttpJsonRequest createJsonRequest(String endpoint, JSONObject jsonObject, HttpRequestMethod method) {
		String url = createEndpointURL(endpoint);
		HttpJsonRequest request = new HttpJsonRequest(url, jsonObject);
		setupRequestDefaults(request);
		request.setMethod(method);
		request.setRequestProperty("Content-Type", "application/json");
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
