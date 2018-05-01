package com.apptentive.android.sdk.network;

import com.apptentive.android.sdk.TestCaseBase;
import com.apptentive.android.sdk.network.MockHttpURLConnection.DefaultResponseHandler;
import com.apptentive.android.sdk.util.threading.MockDispatchQueue;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class HttpRequestManagerTest extends TestCaseBase {

	private HttpRequestManager requestManager;
	private MockDispatchQueue networkQueue;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		networkQueue = new MockDispatchQueue(false);
		requestManager = new MockHttpRequestManager(networkQueue);
	}

	@After
	public void tearDown() {
		requestManager.cancelAll();
		super.tearDown();
	}

	@Test
	public void testStartRequest() {
		startRequest(new MockHttpRequest("1"));
		startRequest(new MockHttpRequest("2").setMockResponseCode(204));
		startRequest(new MockHttpRequest("3").setMockResponseCode(500));
		startRequest(new MockHttpRequest("4").setThrowsExceptionOnConnect(true));
		startRequest(new MockHttpRequest("5").setThrowsExceptionOnDisconnect(true));
		dispatchRequests();

		assertResult(
			"finished: 1",
			"finished: 2",
			"failed: 3 Unexpected response code: 500 (Internal Server Error)",
			"failed: 4 Connection error",
			"failed: 5 Disconnection error"
		);
	}

	@Test
	public void testRequestData() {
		final String expected = "Some test data with Unicode chars 文字";

		final AtomicBoolean finished = new AtomicBoolean(false);

		HttpRequest request = new MockHttpRequest("request").setResponseData(expected);
		request.addListener(new HttpRequest.Adapter<HttpRequest>() {
			@Override
			public void onFinish(HttpRequest request) {
				Assert.assertEquals(expected, request.getResponseData());
				finished.set(true);
			}
		});
		requestManager.startRequest(request);
		dispatchRequests();

		Assert.assertTrue(finished.get());
	}

	@Test
	public void testJsonRequestData() throws JSONException {
		final JSONObject requestObject = new JSONObject();
		requestObject.put("key1", "value1");
		requestObject.put("key2", "value2");
		requestObject.put("key3", "value3");

		final JSONObject expected = new JSONObject();
		expected.put("int", 10);
		expected.put("string", "value");
		expected.put("boolean", true);
		expected.put("float", 3.14f);

		JSONObject inner = new JSONObject();
		inner.put("key", "value");
		expected.put("inner", inner);

		final AtomicBoolean finished = new AtomicBoolean(false);

		HttpJsonRequest request = new MockHttpJsonRequest("request", requestObject).setMockResponseData(expected);
		request.addListener(new HttpRequest.Adapter<HttpJsonRequest>() {
			@Override
			public void onFinish(HttpJsonRequest request) {
				Assert.assertEquals(expected.toString(), request.getResponseObject().toString());
				finished.set(true);
			}

			@Override
			public void onFail(HttpJsonRequest request, String reason) {
				Assert.fail(reason);
			}
		});
		requestManager.startRequest(request);
		dispatchRequests();

		Assert.assertTrue(finished.get());
	}

	@Test
	public void testJsonRequestCorruptedData() throws JSONException {
		final JSONObject requestObject = new JSONObject();
		requestObject.put("key1", "value1");
		requestObject.put("key2", "value2");
		requestObject.put("key3", "value3");

		String invalidJson = "{ key1 : value key2 : value2 }";

		final AtomicBoolean finished = new AtomicBoolean(false);

		HttpJsonRequest request = new MockHttpJsonRequest("request", requestObject).setMockResponseData(invalidJson);
		request.addListener(new HttpRequest.Adapter<HttpJsonRequest>() {
			@Override
			public void onFail(HttpJsonRequest request, String reason) {
				finished.set(true);
			}
		});
		requestManager.startRequest(request);
		dispatchRequests();

		Assert.assertTrue(finished.get());
	}

	@Test
	public void testListener() {
		requestManager.setListener(new HttpRequestManager.Listener() {
			@Override
			public void onRequestStart(HttpRequestManager manager, HttpRequest request) {
				addResult("start: " + request);
			}

			@Override
			public void onRequestFinish(HttpRequestManager manager, HttpRequest request) {
				if (request.isSuccessful()) {
					addResult("finish: " + request);
				} else if (request.isCancelled()) {
					addResult("cancel: " + request);
				} else {
					addResult("fail: " + request);
				}
			}

			@Override
			public void onRequestsCancel(HttpRequestManager manager) {
				addResult("cancel all");
			}
		});


		// start requests and let them finish
		requestManager.startRequest(new MockHttpRequest("1"));
		requestManager.startRequest(new MockHttpRequest("2").setMockResponseCode(500));
		requestManager.startRequest(new MockHttpRequest("3").setThrowsExceptionOnConnect(true));
		dispatchRequests();

		assertResult(
			"start: 1",
			"start: 2",
			"start: 3",
			"finish: 1",
			"fail: 2",
			"fail: 3"
		);

		// start requests and cancel some
		requestManager.startRequest(new MockHttpRequest("4"));
		requestManager.startRequest(new MockHttpRequest("5")).cancel();
		requestManager.startRequest(new MockHttpRequest("6"));
		dispatchRequests();

		assertResult(
			"start: 4",
			"start: 5",
			"start: 6",
			"finish: 4",
			"cancel: 5",
			"finish: 6"
		);

		// start requests and cancel them all
		requestManager.startRequest(new MockHttpRequest("4"));
		requestManager.startRequest(new MockHttpRequest("5"));
		requestManager.startRequest(new MockHttpRequest("6"));
		requestManager.cancelAll();
		dispatchRequests();

		assertResult(
			"start: 4",
			"start: 5",
			"start: 6",
			"cancel all",
			"cancel: 4",
			"cancel: 5",
			"cancel: 6"
		);
	}

	@Test
	public void testFailedRetry() {
		HttpRequestRetryPolicyDefault retryPolicy = new HttpRequestRetryPolicyDefault() {
			@Override
			public boolean shouldRetryRequest(int responseCode, int retryAttempt) {
				return responseCode != -1 && super.shouldRetryRequest(responseCode, retryAttempt); // don't retry on an exception
			}
		};
		retryPolicy.setMaxRetryCount(2);
		retryPolicy.setRetryTimeoutMillis(0);

		startRequest(new MockHttpRequest("1").setMockResponseCode(500).setRetryPolicy(retryPolicy));
		startRequest(new MockHttpRequest("2").setMockResponseCode(400).setRetryPolicy(retryPolicy));
		startRequest(new MockHttpRequest("3").setMockResponseCode(204).setRetryPolicy(retryPolicy));
		startRequest(new MockHttpRequest("4").setThrowsExceptionOnConnect(true).setRetryPolicy(retryPolicy));
		startRequest(new MockHttpRequest("5").setThrowsExceptionOnDisconnect(true).setRetryPolicy(retryPolicy));
		dispatchRequests();

		assertResult(
			"failed: 2 Unexpected response code: 400 (Bad Request)",
			"finished: 3",
			"failed: 4 Connection error",
			"failed: 5 Disconnection error",
			"retried: 1",
			"retried: 1",
			"failed: 1 Unexpected response code: 500 (Internal Server Error)"
		);
	}

	@Test
	public void testSuccessfulRetry() {
		HttpRequestRetryPolicyDefault retryPolicy = new HttpRequestRetryPolicyDefault() {
			@Override
			public boolean shouldRetryRequest(int responseCode, int retryAttempt) {
				return responseCode != -1 && super.shouldRetryRequest(responseCode, retryAttempt); // don't retry on an exception
			}
		};
		retryPolicy.setMaxRetryCount(3);
		retryPolicy.setRetryTimeoutMillis(0);

		// fail this request twice and then finish successfully
		startRequest(new MockHttpRequest("1").setMockResponseHandler(new DefaultResponseHandler() {
			int requestAttempts = 0;

			@Override
			public int getResponseCode() {
				return requestAttempts++ < 2 ? 500 : 200;
			}
		}).setRetryPolicy(retryPolicy));

		startRequest(new MockHttpRequest("2").setMockResponseCode(400).setRetryPolicy(retryPolicy));
		startRequest(new MockHttpRequest("3").setMockResponseCode(204).setRetryPolicy(retryPolicy));
		startRequest(new MockHttpRequest("4").setThrowsExceptionOnConnect(true).setRetryPolicy(retryPolicy));
		startRequest(new MockHttpRequest("5").setThrowsExceptionOnDisconnect(true).setRetryPolicy(retryPolicy));
		dispatchRequests();

		assertResult(
			"failed: 2 Unexpected response code: 400 (Bad Request)",
			"finished: 3",
			"failed: 4 Connection error",
			"failed: 5 Disconnection error",
			"retried: 1",
			"retried: 1",
			"finished: 1"
		);
	}

	//region Helpers

	private void startRequest(HttpRequest request) {
		request.addListener(new HttpRequest.Listener<MockHttpRequest>() {
			@Override
			public void onFinish(MockHttpRequest request) {
				addResult("finished: " + request);
			}

			@Override
			public void onCancel(MockHttpRequest request) {
				addResult("cancelled: " + request);
			}

			@Override
			public void onFail(MockHttpRequest request, String reason) {
				addResult("failed: " + request + " " + reason);
			}
		});

		requestManager.startRequest(request);
	}

	private void dispatchRequests() {
		networkQueue.dispatchTasks();
	}

	//endregion

	//region Mock HttpRequestManager

	private class MockHttpRequestManager extends HttpRequestManager {
		MockHttpRequestManager(MockDispatchQueue networkQueue) {
			super(networkQueue);
		}

		@Override
		void dispatchRequest(HttpRequest request) {
			if (request.retrying) {
				addResult("retried: " + request);
			}
			super.dispatchRequest(request);
		}

		@Override
		synchronized HttpRequest startRequest(HttpRequest request) {
			request.setRequestManager(this);
			return super.startRequest(request);
		}
	}
}