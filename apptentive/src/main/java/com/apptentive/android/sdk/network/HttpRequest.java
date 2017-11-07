/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.network;

import android.util.Base64;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import static com.apptentive.android.sdk.ApptentiveLog.Level.VERY_VERBOSE;
import static com.apptentive.android.sdk.ApptentiveLogTag.*;
import static com.apptentive.android.sdk.debug.Assert.*;

/**
 * Class representing async HTTP request
 */
public class HttpRequest {

	/**
	 * Default retry policy (used if a custom one is not specified)
	 */
	private static final HttpRequestRetryPolicy DEFAULT_RETRY_POLICY = new HttpRequestRetryPolicyDefault();

	/**
	 * Id-number of the next request
	 */
	private static int nextRequestId;

	/**
	 * Parent request manager
	 */
	HttpRequestManager requestManager;

	/**
	 * Url-connection for network communications
	 */
	private HttpURLConnection connection;

	/**
	 * Optional request tag (for an easy request identification)
	 */
	private String tag;

	/**
	 * Optional name of the request (for logging purposes)
	 */
	private String name;

	/**
	 * Request id (used for testing)
	 */
	private final int id;

	/**
	 * URL string of the request
	 */
	private String urlString;

	/**
	 * Request HTTP properties (will be added to a connection)
	 */
	private Map<String, Object> requestProperties;

	/**
	 * Request method (GET, POST, PUT)
	 */
	private HttpRequestMethod method = HttpRequestMethod.GET;

	/**
	 * Connection timeout in milliseconds
	 */
	private int connectTimeout = Constants.DEFAULT_CONNECT_TIMEOUT_MILLIS;

	/**
	 * Read timeout in milliseconds
	 */
	private int readTimeout = Constants.DEFAULT_READ_TIMEOUT_MILLIS;

	/**
	 * The status code from an HTTP response
	 */
	private int responseCode;

	/**
	 * HTTP response content string
	 */
	private String responseData;

	/**
	 * Map of connection response headers
	 */
	private Map<String, String> responseHeaders;

	/**
	 * Cancelled flag (not thread safe)
	 */
	private boolean cancelled;

	/**
	 * Error message for the failed request
	 */
	private String errorMessage;

	/**
	 * Retry policy for this request
	 */
	private HttpRequestRetryPolicy retryPolicy = DEFAULT_RETRY_POLICY;

	/**
	 * How many times request was retried already
	 */
	private int retryAttempt;

	/**
	 * Flag indicating if the request is currently scheduled for a retry
	 */
	boolean retrying;

	@SuppressWarnings("rawtypes")
	private List<Listener> listeners;

	/**
	 * Optional dispatch queue for listener callbacks
	 */
	private DispatchQueue callbackQueue;

	/** Optional injector for debugging purposes */
	private Injector injector;

	public HttpRequest(String urlString) {
		if (urlString == null || urlString.length() == 0) {
			throw new IllegalArgumentException("Invalid URL string '" + urlString + "'");
		}

		this.listeners = new ArrayList<>(1);
		this.id = nextRequestId++;
		this.urlString = urlString;
	}

	////////////////////////////////////////////////////////////////
	// Lifecycle

	public void start() {
		assertNotNull(requestManager);
		if (requestManager != null) {
			requestManager.startRequest(this);
		}
	}

	@SuppressWarnings("unchecked")
	private void finishRequest() {
		try {
			if (isSuccessful()) {
				for (Listener listener : listeners) {
					try {
						listener.onFinish(this);
					} catch (Exception e) {
						ApptentiveLog.e(e, "Exception in request onFinish() listener");
					}
				}
			} else if (isCancelled()) {
				for (Listener listener : listeners) {
					try {
						listener.onCancel(this);
					} catch (Exception e) {
						ApptentiveLog.e(e, "Exception in request onCancel() listener");
					}
				}
			} else {
				for (Listener listener : listeners) {
					try {
						listener.onFail(this, errorMessage);
					} catch (Exception e) {
						ApptentiveLog.e(e, "Exception in request onFail() listener");
					}
				}
			}
		} finally {
			requestManager.unregisterRequest(HttpRequest.this);
		}
	}

	/**
	 * Override this method to create request data on a background thread
	 */
	protected byte[] createRequestData() throws IOException {
		return null;
	}

	/**
	 * Override this method in a subclass to create data from response bytes
	 */
	protected void handleResponse(String response) throws IOException {
	}

	////////////////////////////////////////////////////////////////
	// Request async task

	/**
	 * Send request synchronously on a background network queue
	 */
	void dispatchSync(DispatchQueue networkQueue) {
		long requestStartTime = System.currentTimeMillis();

		try {
			sendRequestSync();
		} catch (NetworkUnavailableException e) {
			responseCode = -1; // indicates failure
			errorMessage = e.getMessage();
			ApptentiveLog.w(e.getMessage());
			ApptentiveLog.w("Cancelled? %b", isCancelled());
		} catch (Exception e) {
			responseCode = -1; // indicates failure
			errorMessage = e.getMessage();
			ApptentiveLog.e("Cancelled? %b", isCancelled());
			if (!isCancelled()) {
				ApptentiveLog.e(e, "Unable to perform request");
			}
		}

		ApptentiveLog.d(NETWORK, "Request finished in %d ms", System.currentTimeMillis() - requestStartTime);

		// attempt a retry if request failed
		if (isFailed() && retryRequest(networkQueue, responseCode)) { // we schedule request retry on the same queue as it was originally dispatched
			return;
		}

		// use custom callback queue (if any)
		if (callbackQueue != null) {
			callbackQueue.dispatchAsync(new DispatchTask() {
				@Override
				protected void execute() {
					finishRequest();
				}
			});
		} else {
			finishRequest(); // we don't care where the callback is dispatched until it's on a background queue
		}
	}

	private void sendRequestSync() throws Exception {
		try {
			if (injector != null) {
				injector.onBeforeSend(this);
			}

			URL url = new URL(urlString);
			ApptentiveLog.d(NETWORK, "Performing request: %s %s", method, url);
			if (ApptentiveLog.canLog(VERY_VERBOSE)) {
				ApptentiveLog.vv(NETWORK, "%s", toString());
			}
			retrying = false;

			connection = openConnection(url);
			connection.setRequestMethod(method.toString());
			connection.setConnectTimeout(connectTimeout);
			connection.setReadTimeout(readTimeout);

			if (!isNetworkConnectionPresent()) {
				ApptentiveLog.d("No network connection present. Request will fail.");
				throw new NetworkUnavailableException("The network is not currently active.");
			}

			if (isCancelled()) {
				return;
			}

			if (requestProperties != null && requestProperties.size() > 0) {
				setupRequestProperties(connection, requestProperties);
			}

			if (!HttpRequestMethod.GET.equals(method)) {
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setUseCaches(false);

				byte[] requestData = createRequestData();
				if (requestData != null && requestData.length > 0) {
					OutputStream outputStream = null;
					try {
						outputStream = connection.getOutputStream();
						outputStream.write(requestData);
					} finally {
						Util.ensureClosed(outputStream);
					}
				}
			}

			// send request
			responseCode = connection.getResponseCode();
			ApptentiveLog.d(NETWORK, "Response: %d %s", responseCode, connection.getResponseMessage());

			if (isCancelled()) {
				return;
			}

			// get HTTP headers
			responseHeaders = getResponseHeaders(connection);

			// TODO: figure out a better way of handling response codes
			boolean gzipped = isGzipContentEncoding(responseHeaders);
			if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
				responseData = readResponse(connection.getInputStream(), gzipped);
				ApptentiveLog.v(NETWORK, "Response data: %s", responseData);
			} else {
				errorMessage = StringUtils.format("Unexpected response code: %d (%s)", responseCode, connection.getResponseMessage());
				responseData = readResponse(connection.getErrorStream(), gzipped);
				ApptentiveLog.w(NETWORK, "Error response data: %s", responseData);
			}

			if (isCancelled()) {
				return;
			}

			if (injector != null) {
				injector.onAfterSend(this);
			}

			// optionally handle response data (should be overridden in a sub class)
			handleResponse(responseData);
		} finally {
			closeConnection();
		}
	}

	protected boolean isNetworkConnectionPresent() {
		return Util.isNetworkConnectionPresent();
	}

	//region Retry

	private final DispatchTask retryDispatchTask = new DispatchTask() {
		@Override
		protected void execute() {
			assertTrue(retrying);
			assertNotNull(requestManager);
			requestManager.dispatchRequest(HttpRequest.this);
		}
	};

	private boolean retryRequest(DispatchQueue networkQueue, int responseCode) {
		assertFalse(retryDispatchTask.isScheduled());

		++retryAttempt;

		if (!retryPolicy.shouldRetryRequest(responseCode, retryAttempt)) {
			ApptentiveLog.v(NETWORK, "Retry policy declined request retry");
			return false;
		}

		retrying = true;
		networkQueue.dispatchAsyncOnce(retryDispatchTask, retryPolicy.getRetryTimeoutMillis(retryAttempt));

		return true;
	}

	//endregion

	//region Connection

	private void setupRequestProperties(HttpURLConnection connection, Map<String, Object> properties) {
		Set<Entry<String, Object>> entries = properties.entrySet();
		for (Entry<String, Object> e : entries) {
			String name = e.getKey();
			Object value = e.getValue();

			if (name != null && value != null) {
				connection.setRequestProperty(name, value.toString());
			}
		}
	}

	/* This method can be overridden in a subclass for customizing or mocking the connection */
	protected HttpURLConnection openConnection(URL url) throws IOException {
		return (HttpURLConnection) url.openConnection();
	}

	private void closeConnection() {
		if (connection != null) {
			connection.disconnect();
			connection = null;
		}
	}

	private static Map<String, String> getResponseHeaders(HttpURLConnection connection) {
		Map<String, String> headers = new HashMap<>();
		Map<String, List<String>> map = connection.getHeaderFields();
		for (Entry<String, List<String>> entry : map.entrySet()) {
			headers.put(entry.getKey(), entry.getValue().toString());
		}
		return headers;
	}

	private static boolean isGzipContentEncoding(Map<String, String> responseHeaders) {
		if (responseHeaders != null) {
			String contentEncoding = responseHeaders.get("Content-Encoding");
			return contentEncoding != null && contentEncoding.equalsIgnoreCase("[gzip]");
		}
		return false;
	}

	private static String readResponse(InputStream is, boolean gzipped) throws IOException {
		if (is == null) {
			return null;
		}

		try {
			if (gzipped) {
				is = new GZIPInputStream(is);
			}
			return Util.readStringFromInputStream(is, "UTF-8");
		} finally {
			Util.ensureClosed(is);
		}
	}

	//endregion

	//region Cancellation

	/**
	 * Returns <code>true</code> if request is cancelled
	 */
	synchronized boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Marks request as cancelled
	 */
	public synchronized void cancel() {
		cancelled = true;
	}

	//endregion

	//region HTTP request properties

	/**
	 * Sets HTTP request property
	 */
	public void setRequestProperty(String key, Object value) {
		if (value != null) {
			if (requestProperties == null) {
				requestProperties = new HashMap<>();
			}
			requestProperties.put(key, value);
		}
	}

	//endregion

	//region String representation

	public String toString() {
		try {
			byte[] requestData = createRequestData();
			String requestString;
			String contentType = requestProperties.get("Content-Type").toString();
			if (contentType.contains("application/octet-stream") || contentType.contains("multipart/encrypted")) {
				requestString = "Base64 encoded binary request: " + Base64.encodeToString(requestData, Base64.NO_WRAP);
			} else {
				requestString = new String(requestData);
			}
			return String.format(
				"\n" +
					"Request:\n" +
					"\t%s %s\n" +
					"\t%s\n" +
					"\t%s\n" +
					"Response:\n" +
					"\t%d\n" +
					"\t%s\n" +
					"\t%s",
				/* Request */
				method.name(), urlString,
				requestProperties,
				requestString,
				/* Response */
				responseCode,
				responseData,
				responseHeaders);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while getting request string representation");
		}
		return null;
	}

	//endregion

	//region Getters/Setters

	public void setRequestManager(HttpRequestManager requestManager) {
		this.requestManager = requestManager;
	}

	public void setMethod(HttpRequestMethod method) {
		if (method == null) {
			throw new IllegalArgumentException("Method is null");
		}

		this.method = method;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public boolean isSuccessful() {
		return responseCode >= 200 && responseCode < 300;
	}

	public boolean isFailed() {
		return !isSuccessful() && !isCancelled();
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public void addListener(Listener<?> listener) {
		if (listener == null) {
			throw new IllegalArgumentException("Listener is null");
		}

		boolean contains = listeners.contains(listener);
		assertFalse(contains, "Already contains listener: %s", listener);
		if (!contains) {
			listeners.add(listener);
		}
	}

	public void setCallbackQueue(DispatchQueue callbackQueue) {
		this.callbackQueue = callbackQueue;
	}

	public String getResponseData() {
		return responseData;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseHeader(String key) {
		return responseHeaders != null ? responseHeaders.get(key) : null;
	}

	public boolean isAuthenticationFailure() {
		return responseCode == 401;
	}

	public Apptentive.AuthenticationFailedReason getAuthenticationFailedReason() {
		if (responseData != null) {
			try {
				JSONObject errorObject = new JSONObject(responseData);
				String error = errorObject.optString("error", null);
				String errorType = errorObject.optString("error_type", null);
				return Apptentive.AuthenticationFailedReason.parse(errorType, error);
			} catch (Exception e) {
				ApptentiveLog.w(e, "Error parsing authentication failure object.");
			}
		}
		return Apptentive.AuthenticationFailedReason.UNKNOWN;
	}

	public HttpRequest setRetryPolicy(HttpRequestRetryPolicy retryPolicy) {
		if (retryPolicy == null) {
			throw new IllegalArgumentException("Retry policy is null");
		}
		this.retryPolicy = retryPolicy;
		return this;
	}

	/* For unit testing */
	protected void setResponseCode(int code) {
		responseCode = code;
	}

	public void setInjector(Injector injector) {
		this.injector = injector;
	}

	//endregion

	//region Listener

	public interface Listener<T extends HttpRequest> {
		void onFinish(T request);

		void onCancel(T request);

		void onFail(T request, String reason);
	}

	public static class Adapter<T extends HttpRequest> implements Listener<T> {

		@Override
		public void onFinish(T request) {

		}

		@Override
		public void onCancel(T request) {

		}

		@Override
		public void onFail(T request, String reason) {

		}
	}

	//endregion

	//region Debug

	public static class Injector {
		public void onBeforeSend(HttpRequest request) throws Exception {
		}

		public void onAfterSend(HttpRequest request) throws Exception {
		}
	}

	//endregion

	public static class NetworkUnavailableException extends IOException {
		public NetworkUnavailableException(String message) {
			super(message);
		}
	}
}
