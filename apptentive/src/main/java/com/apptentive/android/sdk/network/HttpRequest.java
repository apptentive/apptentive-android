package com.apptentive.android.sdk.network;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	 * Optional request URL query params
	 */
	private Map<String, Object> queryParams;

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
	 * The status code from an HTTP response message
	 */
	private int responseCode;

	/**
	 * The status message from an HTTP response
	 */
	private String responseMessage;

	/**
	 * Optional body data
	 */
	private byte[] body;

	/**
	 * Http-response data
	 */
	private byte[] responseData;

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
	 * Total request durationMillis in milliseconds
	 */
	private long durationMillis;

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
				listener.onFinish(this);
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception in request finish listener");
		}
	}

	/**
	 * Override this method in a subclass to create data from response bytes
	 */
	protected void handleResponse(byte[] responseData) throws IOException {
	}

	////////////////////////////////////////////////////////////////
	// Request async task

	void dispatchSync() {
		try {
			sendRequestSync();
		} catch (Exception e) {
			thrownException = e;
			if (!isCancelled()) {
				ApptentiveLog.e(e, "Unable to perform request");
			}
		}

		finishRequest();
	}

	private void sendRequestSync() throws IOException {
		try {
			long startTimestamp = System.currentTimeMillis();

			URL url = createUrl(urlString);
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

				if (body != null && body.length > 0) {
					OutputStream outputStream = null;
					try {
						outputStream = connection.getOutputStream();
						outputStream.write(body);
					} finally {
						if (outputStream != null) {
							outputStream.close();
						}
					}
				}
			}

			responseCode = connection.getResponseCode();
			responseMessage = connection.getResponseMessage();

			if (isCancelled()) {
				return;
			}

			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream is = this.connection.getInputStream();
				responseData = readResponse(is);

				if (isCancelled()) {
					return;
				}

				handleResponse(responseData);

				durationMillis = System.currentTimeMillis() - startTimestamp;
			} else {
				throw new IOException("Unexpected response code " + responseCode + " for URL " + url);
			}
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

	private byte[] readResponse(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int bytesRead;

		while ((bytesRead = is.read(buffer)) != -1) {
			bos.write(buffer, 0, bytesRead);
		}

		return bos.toByteArray();
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

	//region Query

	/**
	 * Sets a URL-parameter. The actual usage depends on request method (GET, POST, etc)
	 */
	public void setQueryParam(String key, Object value) {
		if (value != null) {
			if (queryParams == null) {
				queryParams = new HashMap<>();
			}
			queryParams.put(key, value);
		}
	}

	/**
	 * Sets URL-parameters. The actual usage depends on request method (GET, POST, etc)
	 */
	public void setQueryParams(Map<String, Object> queryParams) {
		if (queryParams != null && queryParams.size() > 0) {
			Set<Entry<String, Object>> entries = queryParams.entrySet();
			for (Entry<String, Object> e : entries) {
				String key = e.getKey();
				if (key == null) {
					throw new IllegalArgumentException("Can't add request param: key is null");
				}

				Object value = e.getValue();
				if (value != null) {
					setQueryParam(key, value);
				}
			}
		}
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

	//region Helpers

	private URL createUrl(String baseUrl) throws IOException {
		if (HttpRequestMethod.GET.equals(method)) {
			if (queryParams != null && queryParams.size() > 0) {
				String query = StringUtils.createQueryString(queryParams);
				if (baseUrl.endsWith("/")) {
					return new URL(baseUrl + query);
				}
				return new URL(baseUrl + "/" + query);
			}
		}
		return new URL(baseUrl);
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

	public byte[] responseData() {
		return responseData;
	}

	public long duration() {
		return durationMillis;
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
	}

	//endregion
}
