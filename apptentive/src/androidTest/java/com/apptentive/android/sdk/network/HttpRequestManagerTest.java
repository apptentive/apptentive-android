package com.apptentive.android.sdk.network;

import com.apptentive.android.sdk.TestCaseBase;
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
	public void setUp() {
		networkQueue = new MockDispatchQueue(false);
		requestManager = new HttpRequestManager(networkQueue);
	}

	@After
	public void tearDown() {
		requestManager.cancelAll();
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
		request.setListener(new HttpRequest.Adapter<HttpRequest>() {
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
		request.setListener(new HttpRequest.Adapter<HttpJsonRequest>() {
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
		request.setListener(new HttpRequest.Adapter<HttpJsonRequest>() {
			@Override
			public void onFail(HttpJsonRequest request, String reason) {
				finished.set(true);
			}
		});
		requestManager.startRequest(request);
		dispatchRequests();

		Assert.assertTrue(finished.get());
	}

	//region Helpers

	private void startRequest(HttpRequest request) {
		request.setListener(new HttpRequest.Listener<MockHttpRequest>() {
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
}