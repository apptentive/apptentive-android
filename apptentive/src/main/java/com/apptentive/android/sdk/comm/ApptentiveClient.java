/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.comm;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static com.apptentive.android.sdk.debug.Assert.notNull;

public class ApptentiveClient {

	private static final String USER_AGENT_STRING = "Apptentive/%s (Android)"; // Format with SDK version string.

	public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 45000;
	public static final int DEFAULT_HTTP_SOCKET_TIMEOUT = 45000;

	// Active API
	private static final String ENDPOINT_MESSAGES = "/conversations/%s/messages?count=%s&after_id=%s&before_id=%s";
	private static final String ENDPOINT_CONFIGURATION = "/conversations/%s/configuration";

	private static final String ENDPOINT_INTERACTIONS = "/conversations/%s/interactions";

	// Deprecated API
	// private static final String ENDPOINT_RECORDS = ENDPOINT_BASE + "/records";
	// private static final String ENDPOINT_SURVEYS_FETCH = ENDPOINT_BASE + "/surveys";

	public static ApptentiveHttpResponse getAppConfiguration() {
		final Conversation conversation = ApptentiveInternal.getInstance().getConversation();
		if (conversation == null) {
			throw new IllegalStateException("Conversation is null");
		}

		final String conversationId = conversation.getConversationId();
		if (conversationId == null) {
			throw new IllegalStateException("Conversation id is null");
		}

		final String conversationToken = conversation.getConversationToken();
		if (conversationToken == null) {
			throw new IllegalStateException("Conversation token is null");
		}

		final String endPoint = StringUtils.format(ENDPOINT_CONFIGURATION, conversationId);
		return performHttpRequest(conversationToken, true, endPoint, Method.GET, null);
	}

	/**
	 * Gets all messages since the message specified by GUID was sent.
	 *
	 * @return An ApptentiveHttpResponse object with the HTTP response code, reason, and content.
	 */
	public static ApptentiveHttpResponse getMessages(Integer count, String afterId, String beforeId) {
		final Conversation conversation = ApptentiveInternal.getInstance().getConversation();
		if (conversation == null) {
			throw new IllegalStateException("Conversation is null");
		}

		final String conversationId = conversation.getConversationId();
		if (conversationId == null) {
			throw new IllegalStateException("Conversation id is null");
		}

		final String conversationToken = conversation.getConversationToken();
		if (conversationToken == null) {
			throw new IllegalStateException("Conversation token is null");
		}

		String uri = String.format(ENDPOINT_MESSAGES, conversationId, count == null ? "" : count.toString(), afterId == null ? "" : afterId, beforeId == null ? "" : beforeId);
		return performHttpRequest(conversationToken, true, uri, Method.GET, null);
	}

	public static ApptentiveHttpResponse getInteractions(String conversationId) {
		if (conversationId == null) {
			throw new IllegalArgumentException("Conversation id is null");
		}
		final String endPoint = StringUtils.format(ENDPOINT_INTERACTIONS, conversationId);
		return performHttpRequest(ApptentiveInternal.getInstance().getConversation().getConversationToken(), true, endPoint, Method.GET, null);
	}

	/**
	 * Perform a Http request.
	 *
	 * @param authToken authorization token for the current connection. Might be an OAuth token for legacy conversations, or Bearer JWT for modern conversations.
	 * @param bearer If true, the token is a bearer JWT, else it is an OAuth token.
	 * @param uri        server url.
	 * @param method     Get/Post/Put
	 * @param body       Data to be POSTed/Put, not used for GET
	 * @return ApptentiveHttpResponse containing content and response returned from the server.
	 */
	private static ApptentiveHttpResponse performHttpRequest(String authToken, boolean bearer, String uri, Method method, String body) {
		uri = getEndpointBase() + uri;
		ApptentiveLog.d("Performing %s request to %s", method.name(), uri);
		//ApptentiveLog.e("OAUTH Token: %s", oauthToken);

		ApptentiveHttpResponse ret = new ApptentiveHttpResponse();
		if (!Util.isNetworkConnectionPresent()) {
			ApptentiveLog.d("Network unavailable.");
			return ret;
		}

		HttpURLConnection connection = null;
		try {
			URL httpUrl = new URL(uri);
			connection = (HttpURLConnection) httpUrl.openConnection();

			connection.setRequestProperty("User-Agent", getUserAgentString());
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setConnectTimeout(DEFAULT_HTTP_CONNECT_TIMEOUT);
			connection.setReadTimeout(DEFAULT_HTTP_SOCKET_TIMEOUT);
			if (bearer) {
				connection.setRequestProperty("Authorization", "Bearer " + authToken);
			} else {
				connection.setRequestProperty("Authorization", "OAuth " + authToken);
			}
			connection.setRequestProperty("Accept-Encoding", "gzip");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("X-API-Version", String.valueOf(Constants.API_VERSION));
			connection.setRequestProperty("APPTENTIVE-KEY", notNull(ApptentiveInternal.getInstance().getApptentiveKey()));
			connection.setRequestProperty("APPTENTIVE-SIGNATURE", notNull(ApptentiveInternal.getInstance().getApptentiveSignature()));

			switch (method) {
				case GET:
					connection.setRequestMethod("GET");
					break;
				case PUT:
					sendPostPutRequest(connection, "PUT", body);
					break;
				case POST:
					sendPostPutRequest(connection, "POST", body);
					break;
				default:
					ApptentiveLog.e("Unrecognized method: " + method.name());
					return ret;
			}

			int responseCode = connection.getResponseCode();
			ret.setCode(responseCode);
			ret.setReason(connection.getResponseMessage());
			ApptentiveLog.d("Response Status Line: " + connection.getResponseMessage());

			// Get the Http response header values
			Map<String, String> headers = new HashMap<String, String>();
			Map<String, List<String>> map = connection.getHeaderFields();
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				headers.put(entry.getKey(), entry.getValue().toString());
			}
			ret.setHeaders(headers);

			// Read the response, if available
			ApptentiveLog.d("HTTP %d: %s", connection.getResponseCode(), connection.getResponseMessage());
			if (responseCode >= 200 && responseCode < 300) {
				ret.setContent(getResponse(connection, ret.isZipped()));
				ApptentiveLog.v("Response: %s", ret.getContent());
			} else {
				ret.setContent(getErrorResponse(connection, ret.isZipped()));
				ApptentiveLog.w("Response: %s", ret.getContent());
			}
		} catch (IllegalArgumentException e) {
			ApptentiveLog.w("Error communicating with server.", e);
		} catch (SocketTimeoutException e) {
			ApptentiveLog.w("Timeout communicating with server.", e);
		} catch (final MalformedURLException e) {
			ApptentiveLog.w("MalformedUrlException", e);
		} catch (final IOException e) {
			ApptentiveLog.w("IOException", e);
			// Read the error response.
			try {
				ret.setContent(getErrorResponse(connection, ret.isZipped()));
				ApptentiveLog.w("Response: " + ret.getContent());
			} catch (IOException ex) {
				ApptentiveLog.w("Can't read error stream.", ex);
			}
		}
		return ret;
	}

	private static void sendPostPutRequest(final HttpURLConnection connection, final String requestMethod, String body) throws IOException {

		ApptentiveLog.d("%s body: %s", requestMethod, body);

		connection.setRequestMethod(requestMethod);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-Type", "application/json");
		if (!TextUtils.isEmpty(body)) {
			BufferedWriter writer = null;
			try {
				OutputStream outputStream = connection.getOutputStream();
				writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
				writer.write(body);
			} finally {
				if (null != writer) {
					writer.flush();
					Util.ensureClosed(writer);
				}
			}
		}
	}

	private enum Method {
		GET,
		PUT,
		POST
	}

	public static String getUserAgentString() {
		return String.format(USER_AGENT_STRING, Constants.APPTENTIVE_SDK_VERSION);
	}

	private static String getEndpointBase() {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
		String url = prefs.getString(Constants.PREF_KEY_SERVER_URL, null);
		if (url == null) {
			url = ApptentiveInternal.getInstance().getServerUrl();
			prefs.edit().putString(Constants.PREF_KEY_SERVER_URL, url).apply();
		}
		return url;
	}

	/**
	 * Reads response and returns it as a string. Handles gzipped streams.
	 *
	 * @param connection Current connection
	 * @return Response as String
	 * @throws IOException
	 */
	public static String getResponse(HttpURLConnection connection, boolean isZipped) throws IOException {
		if (connection != null) {
			InputStream is = null;
			try {
				is = connection.getInputStream();
				if (is != null) {
					if (isZipped) {
						is = new GZIPInputStream(is);
					}
					return Util.readStringFromInputStream(is, "UTF-8");
				}
			} finally {
				Util.ensureClosed(is);
			}
		}
		return null;
	}

	/**
	 * Reads error response and returns it as a string. Handles gzipped streams.
	 *
	 * @param connection Current connection
	 * @return Error response as String
	 * @throws IOException
	 */
	public static String getErrorResponse(HttpURLConnection connection, boolean isZipped) throws IOException {
		if (connection != null) {
			InputStream is = null;
			try {
				is = connection.getErrorStream();
				if (is != null) {
					if (isZipped) {
						is = new GZIPInputStream(is);
					}
				}
				return Util.readStringFromInputStream(is, "UTF-8");
			} finally {
				Util.ensureClosed(is);
			}
		}
		return null;
	}
}
