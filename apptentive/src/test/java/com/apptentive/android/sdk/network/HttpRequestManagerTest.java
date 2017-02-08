package com.apptentive.android.sdk.network;

import com.apptentive.android.sdk.TestCaseBase;
import com.apptentive.android.sdk.util.threading.MockDispatchQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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