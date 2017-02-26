/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.comm;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.CompoundMessage;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.image.ImageUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class ApptentiveClient {

	public static final int API_VERSION = 7;

	private static final String USER_AGENT_STRING = "Apptentive/%s (Android)"; // Format with SDK version string.

	public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 45000;
	public static final int DEFAULT_HTTP_SOCKET_TIMEOUT = 45000;

	// Active API
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

	public static ApptentiveHttpResponse getAppConfiguration() {
		return performHttpRequest(ApptentiveInternal.getInstance().getConversation().getConversationToken(), ENDPOINT_CONFIGURATION, Method.GET, null);
	}

	/**
	 * Gets all messages since the message specified by GUID was sent.
	 *
	 * @return An ApptentiveHttpResponse object with the HTTP response code, reason, and content.
	 */
	public static ApptentiveHttpResponse getMessages(Integer count, String afterId, String beforeId) {
		String uri = String.format(ENDPOINT_CONVERSATION_FETCH, count == null ? "" : count.toString(), afterId == null ? "" : afterId, beforeId == null ? "" : beforeId);
		return performHttpRequest(ApptentiveInternal.getInstance().getConversation().getConversationToken(), uri, Method.GET, null);
	}

	public static ApptentiveHttpResponse postMessage(ApptentiveMessage apptentiveMessage) {
		switch (apptentiveMessage.getType()) {
			case CompoundMessage: {
				CompoundMessage compoundMessage = (CompoundMessage) apptentiveMessage;
				List<StoredFile> associatedFiles = compoundMessage.getAssociatedFiles();
				return performMultipartFilePost(ApptentiveInternal.getInstance().getConversation().getConversationToken(), ENDPOINT_MESSAGES, apptentiveMessage.marshallForSending(), associatedFiles);
			}
			case unknown:
				break;
		}
		return new ApptentiveHttpResponse();
	}

	public static ApptentiveHttpResponse postEvent(Event event) {
		return performHttpRequest(ApptentiveInternal.getInstance().getConversation().getConversationToken(), ENDPOINT_EVENTS, Method.POST, event.marshallForSending());
	}

	public static ApptentiveHttpResponse putDevice(Device device) {
		return performHttpRequest(ApptentiveInternal.getInstance().getConversation().getConversationToken(), ENDPOINT_DEVICES, Method.PUT, device.marshallForSending());
	}

	public static ApptentiveHttpResponse putSdk(Sdk sdk) {
		return performHttpRequest(ApptentiveInternal.getInstance().getConversation().getConversationToken(), ENDPOINT_CONVERSATION, Method.PUT, sdk.marshallForSending());
	}

	public static ApptentiveHttpResponse putAppRelease(AppRelease appRelease) {
		return performHttpRequest(ApptentiveInternal.getInstance().getConversation().getConversationToken(), ENDPOINT_CONVERSATION, Method.PUT, appRelease.marshallForSending());
	}

	public static ApptentiveHttpResponse putSdkAndAppRelease(SdkAndAppReleasePayload payload) {
		return performHttpRequest(ApptentiveInternal.getInstance().getConversation().getConversationToken(), ENDPOINT_CONVERSATION, Method.PUT, payload.marshallForSending());
	}

	public static ApptentiveHttpResponse putPerson(Person person) {
		return performHttpRequest(ApptentiveInternal.getInstance().getConversation().getConversationToken(), ENDPOINT_PEOPLE, Method.PUT, person.marshallForSending());
	}

	public static ApptentiveHttpResponse postSurvey(SurveyResponse survey) {
		String endpoint = String.format(ENDPOINT_SURVEYS_POST, survey.getId());
		return performHttpRequest(ApptentiveInternal.getInstance().getConversation().getConversationToken(), endpoint, Method.POST, survey.marshallForSending());
	}

	public static ApptentiveHttpResponse getInteractions() {
		return performHttpRequest(ApptentiveInternal.getInstance().getConversation().getConversationToken(), ENDPOINT_INTERACTIONS, Method.GET, null);
	}

	/**
	 * Perform a Http request.
	 *
	 * @param oauthToken authorization token for the current connection
	 * @param uri        server url.
	 * @param method     Get/Post/Put
	 * @param body       Data to be POSTed/Put, not used for GET
	 * @return ApptentiveHttpResponse containing content and response returned from the server.
	 */
	private static ApptentiveHttpResponse performHttpRequest(String oauthToken, String uri, Method method, String body) {
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

	private static ApptentiveHttpResponse performMultipartFilePost(String oauthToken, String uri, String postBody, List<StoredFile> associatedFiles) {
		uri = getEndpointBase() + uri;
		ApptentiveLog.d("Performing multipart POST to %s", uri);
		ApptentiveLog.d("Multipart POST body: %s", postBody);

		ApptentiveHttpResponse ret = new ApptentiveHttpResponse();
		if (!Util.isNetworkConnectionPresent()) {
			ApptentiveLog.d("Network unavailable.");
			return ret;
		}

		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = UUID.randomUUID().toString();

		HttpURLConnection connection = null;
		DataOutputStream os = null;

		try {

			// Set up the request.
			URL url = new URL(uri);
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setConnectTimeout(DEFAULT_HTTP_CONNECT_TIMEOUT);
			connection.setReadTimeout(DEFAULT_HTTP_SOCKET_TIMEOUT);
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Content-Type", "multipart/mixed;boundary=" + boundary);
			connection.setRequestProperty("Authorization", "OAuth " + oauthToken);
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("X-API-Version", String.valueOf(API_VERSION));
			connection.setRequestProperty("User-Agent", getUserAgentString());

			// Open an output stream.
			os = new DataOutputStream(connection.getOutputStream());
			os.writeBytes(twoHyphens + boundary + lineEnd);

			// Write text message
			os.writeBytes("Content-Disposition: form-data; name=\"message\"" + lineEnd);
			// Indicate the character encoding is UTF-8
			os.writeBytes("Content-Type: text/plain;charset=UTF-8" + lineEnd);

			os.writeBytes(lineEnd);
			// Explicitly encode message json in utf-8
			os.write(postBody.getBytes("UTF-8"));
			os.writeBytes(lineEnd);


			// Send associated files
			if (associatedFiles != null) {
				for (StoredFile storedFile : associatedFiles) {
					FileInputStream fis = null;
					try {
						String cachedImagePathString = storedFile.getLocalFilePath();
						String originalFilePath = storedFile.getSourceUriOrPath();
						File cachedImageFile = new File(cachedImagePathString);
						// No local cache found
						if (!cachedImageFile.exists()) {
							boolean bCachedCreated = false;
							if (Util.isMimeTypeImage(storedFile.getMimeType())) {
								// Create a scaled down version of original image
								bCachedCreated = ImageUtil.createScaledDownImageCacheFile(originalFilePath, cachedImagePathString);
							} else {
								// For non-image file, just copy to a cache file
								if (Util.createLocalStoredFile(originalFilePath, cachedImagePathString, null) != null) {
									bCachedCreated = true;
								}
							}

							if (!bCachedCreated) {
								continue;
							}
						}
						os.writeBytes(twoHyphens + boundary + lineEnd);
						StringBuilder requestText = new StringBuilder();
						String fileFullPathName = originalFilePath;
						if (TextUtils.isEmpty(fileFullPathName)) {
							fileFullPathName = cachedImagePathString;
						}
						requestText.append(String.format("Content-Disposition: form-data; name=\"file[]\"; filename=\"%s\"", fileFullPathName)).append(lineEnd);
						requestText.append("Content-Type: ").append(storedFile.getMimeType()).append(lineEnd);
						// Write file attributes
						os.writeBytes(requestText.toString());
						os.writeBytes(lineEnd);

						fis = new FileInputStream(cachedImageFile);

						int bytesAvailable = fis.available();
						int maxBufferSize = 512 * 512;
						int bufferSize = Math.min(bytesAvailable, maxBufferSize);
						byte[] buffer = new byte[bufferSize];

						// read image data 0.5MB at a time and write it into buffer
						int bytesRead = fis.read(buffer, 0, bufferSize);
						while (bytesRead > 0) {
							os.write(buffer, 0, bufferSize);
							bytesAvailable = fis.available();
							bufferSize = Math.min(bytesAvailable, maxBufferSize);
							bytesRead = fis.read(buffer, 0, bufferSize);
						}
					} catch (IOException e) {
						ApptentiveLog.d("Error writing file bytes to HTTP connection.", e);
						ret.setBadPayload(true);
						throw e;
					} finally {
						Util.ensureClosed(fis);
					}
					os.writeBytes(lineEnd);
				}
			}
			os.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			os.flush();
			os.close();

			ret.setCode(connection.getResponseCode());
			ret.setReason(connection.getResponseMessage());

			// Read the normal response.
			InputStream responseInputStream = null;
			ByteArrayOutputStream byteArrayOutputStream = null;
			try {
				responseInputStream = connection.getInputStream();
				byteArrayOutputStream = new ByteArrayOutputStream();
				byte[] eBuf = new byte[1024];
				int eRead;
				while (responseInputStream != null && (eRead = responseInputStream.read(eBuf, 0, 1024)) > 0) {
					byteArrayOutputStream.write(eBuf, 0, eRead);
				}
				ret.setContent(byteArrayOutputStream.toString());
			} finally {
				Util.ensureClosed(responseInputStream);
				Util.ensureClosed(byteArrayOutputStream);
			}

			ApptentiveLog.d("HTTP %d: %s", connection.getResponseCode(), connection.getResponseMessage());
			ApptentiveLog.v("Response: %s", ret.getContent());
		} catch (FileNotFoundException e) {
			ApptentiveLog.e("Error getting file to upload.", e);
		} catch (MalformedURLException e) {
			ApptentiveLog.e("Error constructing url for file upload.", e);
		} catch (SocketTimeoutException e) {
			ApptentiveLog.w("Timeout communicating with server.");
		} catch (IOException e) {
			ApptentiveLog.e("Error executing file upload.", e);
			try {
				ret.setContent(getErrorResponse(connection, ret.isZipped()));
			} catch (IOException ex) {
				ApptentiveLog.w("Can't read error stream.", ex);
			}
		} finally {
			Util.ensureClosed(os);
		}
		return ret;
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
