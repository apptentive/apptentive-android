/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.comm;

import android.content.Context;
import android.text.TextUtils;

import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.OutgoingFileMessage;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * @author Sky Kelsey
 */
public class ApptentiveClient {

	private static final int API_VERSION = 3;

	private static final String USER_AGENT_STRING = "Apptentive/%s (Android)"; // Format with SDK version string.

	private static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 30000;
	private static final int DEFAULT_HTTP_SOCKET_TIMEOUT = 30000;

	// Active API
	private static final String ENDPOINT_BASE_STAGING = "https://api.apptentive-beta.com";
	private static final String ENDPOINT_BASE_PRODUCTION = "https://api.apptentive.com";
	private static final String ENDPOINT_CONVERSATION = "/conversation";
	private static final String ENDPOINT_CONVERSATION_FETCH = ENDPOINT_CONVERSATION + "?count=%s&after_id=%s&before_id=%s";
	private static final String ENDPOINT_MESSAGES = "/messages";
	private static final String ENDPOINT_EVENTS = "/events";
	private static final String ENDPOINT_DEVICES = "/devices";
	private static final String ENDPOINT_PEOPLE = "/people";
	private static final String ENDPOINT_CONFIGURATION = ENDPOINT_CONVERSATION + "/configuration";
	private static final String ENDPOINT_SURVEYS_POST = "/surveys/%s/respond";

	private static final String ENDPOINT_INTERACTIONS = "/interactions";

	// Deprecated API
	// private static final String ENDPOINT_RECORDS = ENDPOINT_BASE + "/records";
	// private static final String ENDPOINT_SURVEYS_FETCH = ENDPOINT_BASE + "/surveys";

	public static boolean useStagingServer = false;

	public static ApptentiveHttpResponse getConversationToken(Context appContext, ConversationTokenRequest conversationTokenRequest) {
		return performHttpRequest(appContext, GlobalInfo.apiKey, ENDPOINT_CONVERSATION, Method.POST, conversationTokenRequest.toString());
	}

	public static ApptentiveHttpResponse getAppConfiguration(Context appContext) {
		return performHttpRequest(appContext, GlobalInfo.conversationToken, ENDPOINT_CONFIGURATION, Method.GET, null);
	}

	/**
	 * Gets all messages since the message specified by guid was sent.
	 *
	 * @return An ApptentiveHttpResponse object with the HTTP response code, reason, and content.
	 */
	public static ApptentiveHttpResponse getMessages(Context appContext, Integer count, String afterId, String beforeId) {
		String uri = String.format(ENDPOINT_CONVERSATION_FETCH, count == null ? "" : count.toString(), afterId == null ? "" : afterId, beforeId == null ? "" : beforeId);
		return performHttpRequest(appContext, GlobalInfo.conversationToken, uri, Method.GET, null);
	}

	public static ApptentiveHttpResponse postMessage(Context context, ApptentiveMessage apptentiveMessage) {
		switch (apptentiveMessage.getType()) {
			case TextMessage:
				return performHttpRequest(context, GlobalInfo.conversationToken, ENDPOINT_MESSAGES, Method.POST, apptentiveMessage.marshallForSending());
			case AutomatedMessage:
				return performHttpRequest(context, GlobalInfo.conversationToken, ENDPOINT_MESSAGES, Method.POST, apptentiveMessage.marshallForSending());
			case FileMessage:
				OutgoingFileMessage fileMessage = (OutgoingFileMessage) apptentiveMessage;
				StoredFile storedFile = fileMessage.getStoredFile(context);
				return performMultipartFilePost(context, GlobalInfo.conversationToken, ENDPOINT_MESSAGES, apptentiveMessage.marshallForSending(), storedFile);
			case unknown:
				break;
		}
		return new ApptentiveHttpResponse();
	}

	public static ApptentiveHttpResponse postEvent(Context appContext, Event event) {
		return performHttpRequest(appContext, GlobalInfo.conversationToken, ENDPOINT_EVENTS, Method.POST, event.marshallForSending());
	}

	public static ApptentiveHttpResponse putDevice(Context appContext, Device device) {
		return performHttpRequest(appContext, GlobalInfo.conversationToken, ENDPOINT_DEVICES, Method.PUT, device.marshallForSending());
	}

	public static ApptentiveHttpResponse putSdk(Context appContext, Sdk sdk) {
		return performHttpRequest(appContext, GlobalInfo.conversationToken, ENDPOINT_CONVERSATION, Method.PUT, sdk.marshallForSending());
	}

	public static ApptentiveHttpResponse putAppRelease(Context appContext, AppRelease appRelease) {
		return performHttpRequest(appContext, GlobalInfo.conversationToken, ENDPOINT_CONVERSATION, Method.PUT, appRelease.marshallForSending());
	}

	public static ApptentiveHttpResponse putPerson(Context appContext, Person person) {
		return performHttpRequest(appContext, GlobalInfo.conversationToken, ENDPOINT_PEOPLE, Method.PUT, person.marshallForSending());
	}

	public static ApptentiveHttpResponse postSurvey(Context appContext, SurveyResponse survey) {
		String endpoint = String.format(ENDPOINT_SURVEYS_POST, survey.getId());
		return performHttpRequest(appContext, GlobalInfo.conversationToken, endpoint, Method.POST, survey.marshallForSending());
	}

	public static ApptentiveHttpResponse getInteractions(Context appContext) {
		return performHttpRequest(appContext, GlobalInfo.conversationToken, ENDPOINT_INTERACTIONS, Method.GET, null);
	}

	/**
	 * Perform a Http request.
	 *
	 * @param appContext The ApplicationContext from which this method is called.
	 * @param oauthToken authorization token for the current connection
	 * @param uri        server url.
	 * @param method     Get/Post/Put
	 * @param body       Data to be POSTed/Put, not used for GET
	 * @return ApptentiveHttpResponse containg content and response returned from the server.
	 */
	private static ApptentiveHttpResponse performHttpRequest(Context appContext, String oauthToken, String uri, Method method, String body) {
		uri = getEndpointBase() + uri;
		Log.d("Performing request to %s", uri);
		//Log.e("OAUTH Token: %s", oauthToken);

		ApptentiveHttpResponse ret = new ApptentiveHttpResponse();
		if (!Util.isNetworkConnectionPresent(appContext)) {
			Log.d("Network unavailable.");
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
			connection.setRequestProperty("Authorization", "OAuth " + oauthToken);
			connection.setRequestProperty("Accept-Encoding", "gzip");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("X-API-Version", String.valueOf(API_VERSION));

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
					Log.e("Unrecognized method: " + method.name());
					return ret;
			}

			int responseCode = connection.getResponseCode();
			ret.setCode(responseCode);
			ret.setReason(connection.getResponseMessage());
			Log.d("Response Status Line: " + connection.getResponseMessage());

			// Get the Http response header values
			Map<String, String> headers = new HashMap<String, String>();
			Map<String, List<String>> map = connection.getHeaderFields();
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				headers.put(entry.getKey(), entry.getValue().toString());
			}
			ret.setHeaders(headers);

			// Read the normal content response
			InputStream nis = null;
			try {
				nis = connection.getInputStream();
				if (nis != null) {
					String contentEncoding = ret.getHeaders().get("Content-Encoding");
					if (contentEncoding != null && contentEncoding.equalsIgnoreCase("[gzip]")) {
						nis = new GZIPInputStream(nis);
					}
					ret.setContent(Util.readStringFromInputStream(nis, "UTF-8"));
					if (responseCode >= 200 && responseCode < 300) {
						Log.v("Response: " + ret.getContent());
					} else {
						Log.w("Response: " + ret.getContent());
					}
				}
			} finally {
				Util.ensureClosed(nis);
			}

		} catch (IllegalArgumentException e) {
			Log.w("Error communicating with server.", e);
		} catch (SocketTimeoutException e) {
			Log.w("Timeout communicating with server.", e);
		} catch (final MalformedURLException e) {
			Log.w("ClientProtocolException", e);
		} catch (final IOException e) {
			Log.w("ClientProtocolException", e);
			// Read the error response.
			try {
				ret.setContent(getErrorInResponse(connection));
			} catch (IOException ex) {
				Log.w("Can't read error stream.", ex);
			}
		}
		return ret;
	}

	private static void sendPostPutRequest(final HttpURLConnection connection, final String requestMethod, String body) throws IOException {

		Log.d("%s body: %s", requestMethod, body);

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

	/**
	 * Reads error message from response and returns it as a string.
	 *
	 * @param con current connection
	 * @return String with error message contents.
	 * @throws IOException
	 */
	private static String getErrorInResponse(HttpURLConnection con) throws IOException {
		assert (con != null);
		BufferedReader in = null;
		StringBuffer errStream = null;
		try {
			errStream = new StringBuffer();
			InputStream errorStream = con.getErrorStream();
			assert (errorStream != null);
			in = new BufferedReader(new InputStreamReader(errorStream));
			String errStr;
			while ((errStr = in.readLine()) != null) {
				errStream.append(errStr);
			}
		} finally {
			Util.ensureClosed(in);
		}
		return (errStream != null) ? (errStream.toString()) : null;
	}

	private static ApptentiveHttpResponse performMultipartFilePost(Context appContext, String oauthToken, String uri, String postBody, StoredFile storedFile) {
		uri = getEndpointBase() + uri;
		Log.d("Performing multipart request to %s", uri);

		ApptentiveHttpResponse ret = new ApptentiveHttpResponse();
		if (!Util.isNetworkConnectionPresent(appContext)) {
			Log.d("Network unavailable.");
			return ret;
		}

		if (storedFile == null) {
			Log.e("StoredFile is null. Unable to send.");
			return ret;
		}

		int bytesRead;
		int bufferSize = 4096;
		byte[] buffer;

		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = UUID.randomUUID().toString();

		HttpURLConnection connection = null;
		DataOutputStream os = null;
		InputStream is = null;

		try {
			is = appContext.openFileInput(storedFile.getLocalFilePath());

			// Set up the request.
			URL url = new URL(uri);
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setConnectTimeout(DEFAULT_HTTP_CONNECT_TIMEOUT);
			connection.setReadTimeout(DEFAULT_HTTP_SOCKET_TIMEOUT);
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			connection.setRequestProperty("Authorization", "OAuth " + oauthToken);
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("X-API-Version", String.valueOf(API_VERSION));
			connection.setRequestProperty("User-Agent", getUserAgentString());

			StringBuilder requestText = new StringBuilder();

			// Write form data
			requestText.append(twoHyphens).append(boundary).append(lineEnd);
			requestText.append("Content-Disposition: form-data; name=\"message\"").append(lineEnd);
			requestText.append("Content-Type: text/plain").append(lineEnd);
			requestText.append(lineEnd);
			requestText.append(postBody);
			requestText.append(lineEnd);

			// Write file attributes.
			requestText.append(twoHyphens).append(boundary).append(lineEnd);
			requestText.append(String.format("Content-Disposition: form-data; name=\"file\"; filename=\"%s\"", storedFile.getFileName())).append(lineEnd);
			requestText.append("Content-Type: ").append(storedFile.getMimeType()).append(lineEnd);
			requestText.append(lineEnd);

			Log.d("Post body: " + requestText);

			// Open an output stream.
			os = new DataOutputStream(connection.getOutputStream());

			// Write the text so far.
			os.writeBytes(requestText.toString());

			try {
				// Write the actual file.
				buffer = new byte[bufferSize];
				while ((bytesRead = is.read(buffer, 0, bufferSize)) > 0) {
					os.write(buffer, 0, bytesRead);
				}
			} catch (IOException e) {
				Log.d("Error writing file bytes to HTTP connection.", e);
				ret.setBadPayload(true);
				throw e;
			}

			os.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
			os.close();

			ret.setCode(connection.getResponseCode());
			ret.setReason(connection.getResponseMessage());

			// TODO: These streams may not be ready to read now. Put this in a new thread.
			// Read the normal response.
			InputStream nis = null;
			ByteArrayOutputStream nbaos = null;
			try {
				Log.d("Sending file: " + storedFile.getLocalFilePath());
				nis = connection.getInputStream();
				nbaos = new ByteArrayOutputStream();
				byte[] eBuf = new byte[1024];
				int eRead;
				while (nis != null && (eRead = nis.read(eBuf, 0, 1024)) > 0) {
					nbaos.write(eBuf, 0, eRead);
				}
				ret.setContent(nbaos.toString());
			} finally {
				Util.ensureClosed(nis);
				Util.ensureClosed(nbaos);
			}

			Log.d("HTTP " + connection.getResponseCode() + ": " + connection.getResponseMessage() + "");
			Log.v(ret.getContent());
		} catch (FileNotFoundException e) {
			Log.e("Error getting file to upload.", e);
		} catch (MalformedURLException e) {
			Log.e("Error constructing url for file upload.", e);
		} catch (SocketTimeoutException e) {
			Log.w("Timeout communicating with server.");
		} catch (IOException e) {
			Log.e("Error executing file upload.", e);
			try {
				ret.setContent(getErrorInResponse(connection));
			} catch (IOException ex) {
				Log.w("Can't read error stream.", ex);
			}
		} finally {
			Util.ensureClosed(is);
			Util.ensureClosed(os);
		}
		return ret;
	}

	private enum Method {
		GET,
		PUT,
		POST
	}

	private static String getUserAgentString() {
		return String.format(USER_AGENT_STRING, Constants.APPTENTIVE_SDK_VERSION);
	}

	private static String getEndpointBase() {
		return useStagingServer ? ENDPOINT_BASE_STAGING : ENDPOINT_BASE_PRODUCTION;
	}
}
