package com.apptentive.android.sdk.network;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import static com.apptentive.android.sdk.ApptentiveLogTag.NETWORK;

/**
 * Class representing async HTTP request
 */
public class HttpRequest {

	/**
	 * Default connection timeout
	 */
	private static final long DEFAULT_CONNECT_TIMEOUT_MILLIS = 45 * 1000L;

	/**
	 * Default read timeout
	 */
	private static final long DEFAULT_READ_TIMEOUT_MILLIS = 45 * 1000L;

	/**
	 * Id-number of the next request
	 */
	private static int nextRequestId;

	/**
	 * Url-connection for network communications
	 */
	private HttpURLConnection connection;

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
	private long connectTimeout = DEFAULT_CONNECT_TIMEOUT_MILLIS;

	/**
	 * Read timeout in milliseconds
	 */
	private long readTimeout = DEFAULT_READ_TIMEOUT_MILLIS;

	/**
	 * The status code from an HTTP response
	 */
	private int responseCode;

	/**
	 * The status message from an HTTP response
	 */
	private String responseMessage;

	/**
	 * HTTP response content string
	 */
	private String responseContent;

	/**
	 * Map of connection response headers
	 */
	private Map<String, String> responseHeaders;

	/**
	 * Cancelled flag (not thread safe)
	 */
	private boolean cancelled;

	/**
	 * Inner exception thrown on a background thread
	 */
	private Exception thrownException;

	@SuppressWarnings("rawtypes")
	private Listener listener;

	/**
	 * Optional dispatch queue for listener callbacks
	 */
	private DispatchQueue callbackQueue;

	public HttpRequest(String urlString) {
		if (urlString == null || urlString.length() == 0) {
			throw new IllegalArgumentException("Invalid URL string '" + urlString + "'");
		}

		this.id = nextRequestId++;
		this.urlString = urlString;
	}

	////////////////////////////////////////////////////////////////
	// Lifecycle

	@SuppressWarnings("unchecked")
	private void finishRequest() {
		try {
			if (listener != null) {
				if (isSuccessful()) {
					listener.onFinish(this);
				} else if (isCancelled()) {
					listener.onCancel(this);
				} else {
					listener.onFail(this, thrownException != null ? thrownException.getMessage() : null);
				}
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception in request finish listener");
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

	void dispatchSync() {
		long requestStartTime = System.currentTimeMillis();

		try {
			sendRequestSync();
		} catch (Exception e) {
			thrownException = e;
			if (!isCancelled()) {
				ApptentiveLog.e(e, "Unable to perform request");
			}
		}

		ApptentiveLog.d(NETWORK, "Request finished in %d ms", System.currentTimeMillis() - requestStartTime);

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

	private void sendRequestSync() throws IOException {
		try {
			URL url = new URL(urlString);
			ApptentiveLog.d(NETWORK, "Performing request: %s", url);

			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(method.toString());
			connection.setConnectTimeout((int) connectTimeout);
			connection.setReadTimeout((int) readTimeout);

			if (isCancelled()) {
				return;
			}

			if (requestProperties != null && requestProperties.size() > 0) {
				setupRequestProperties(connection, requestProperties);
			}

			if (HttpRequestMethod.POST.equals(method) || HttpRequestMethod.PUT.equals(method)) {
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
			responseMessage = connection.getResponseMessage();

			if (isCancelled()) {
				return;
			}

			// get HTTP headers
			responseHeaders = getResponseHeaders(connection);

			// TODO: figure out a better way of handling response codes
			boolean gzipped = isGzipContentEncoding(responseHeaders);
			if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
				responseContent = readResponse(connection.getInputStream(), gzipped);
				ApptentiveLog.v(NETWORK, "Response: %s", responseContent);
			} else {
				responseContent = readResponse(connection.getErrorStream(), gzipped);
				ApptentiveLog.w(NETWORK, "Response: %s", responseContent);
			}

			if (isCancelled()) {
				return;
			}

			// optionally handle response data (should be overridden in a sub class)
			handleResponse(responseContent);
		} finally {
			closeConnection();
		}
	}

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
	public synchronized boolean isCancelled() {
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
		return urlString;
	}

	//endregion

	//region Getters/Setters

	public void setMethod(HttpRequestMethod method) {
		if (method == null) {
			throw new IllegalArgumentException("Method is null");
		}

		this.method = method;
	}

	public void setConnectTimeout(long connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public void setReadTimeout(long readTimeout) {
		this.readTimeout = readTimeout;
	}

	public boolean isSuccessful() {
		return responseCode >= 200 && responseCode < 300;
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

	public void setListener(Listener<?> listener) {
		this.listener = listener;
	}

	public void setCallbackQueue(DispatchQueue callbackQueue) {
		this.callbackQueue = callbackQueue;
	}

	/**
	 * Inner exception which caused request to fail
	 */
	public Exception getThrownException() {
		return thrownException;
	}

	//endregion

	//region Listener

	public interface Listener<T> {
		void onFinish(T request);

		void onCancel(T request);

		void onFail(T request, String reason);
	}

	public static abstract class Adapter<T> implements Listener<T> {

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
}
