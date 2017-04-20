/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.TestCaseBase;
import com.apptentive.android.sdk.model.JsonPayload;
import com.apptentive.android.sdk.model.Payload;
import com.apptentive.android.sdk.model.PayloadType;
import com.apptentive.android.sdk.network.HttpRequest;
import com.apptentive.android.sdk.network.HttpRequestManager;
import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.network.HttpRequestRetryPolicyDefault;
import com.apptentive.android.sdk.network.MockHttpRequest;
import com.apptentive.android.sdk.network.MockHttpURLConnection.DefaultResponseHandler;
import com.apptentive.android.sdk.network.MockHttpURLConnection.ResponseHandler;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.threading.MockDispatchQueue;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class JsonPayloadSenderTest extends TestCaseBase {
	private MockDispatchQueue networkQueue;

	@Before
	public void setUp() {
		super.setUp();
		networkQueue = new MockDispatchQueue(false);
	}

	@Test
	public void testSendPayload() throws Exception {

		final MockPayloadRequestSender requestSender = new MockPayloadRequestSender();

		PayloadSender sender = new PayloadSender(requestSender, new HttpRequestRetryPolicyDefault());
		sender.setListener(new PayloadSender.Listener() {
			@Override
			public void onFinishSending(PayloadSender sender, Payload payload, boolean cancelled, String errorMessage) {
				if (cancelled) {
					addResult("cancelled: " + payload);
				} else if (errorMessage != null) {
					addResult("failed: " + payload + " " + errorMessage);
				} else {
					addResult("succeed: " + payload);
				}
			}
		});

		final MockPayload payload1 = new MockPayload("key1", "value1");
		final MockPayload payload2 = new MockPayload("key2", "value2").setResponseHandler(new DefaultResponseHandler() {
			int retryAttempt = 0;

			@Override
			public int getResponseCode() {
				return ++retryAttempt > 1 ? 200 : 500;
			}
		});
		final MockPayload payload3 = new MockPayload("key3", "value3").setResponseCode(400);

		assertTrue(sender.sendPayload(payload1));
		assertFalse(sender.sendPayload(payload2)); // would not start sending until the first one is complete
		assertFalse(sender.sendPayload(payload3)); // would not start sending until the first one is complete

		networkQueue.dispatchTasks();
		assertResult(
			"succeed: {'key1':'value1'}"
		);

		assertTrue(sender.sendPayload(payload2));
		assertFalse(sender.sendPayload(payload3)); // would not start sending until the first one is complete

		networkQueue.dispatchTasks();
		assertResult(
			"succeed: {'key2':'value2'}" // NOTE: this request would succeed on the second attempt
		);

		assertTrue(sender.sendPayload(payload3));

		networkQueue.dispatchTasks();
		assertResult(
			"failed: {'key3':'value3'} Unexpected response code: 400 (Bad Request)"
		);
	}

	class MockPayload extends JsonPayload {
		private final String json;
		private ResponseHandler responseHandler;

		public MockPayload(String key, Object value) {
			json = StringUtils.format("{'%s':'%s'}", key, value);
			responseHandler = new DefaultResponseHandler();
			setDatabaseId(0L);
		}

		@Override
		protected void initPayloadType() {
			setPayloadType(PayloadType.event);
		}

		public MockPayload setResponseCode(int responseCode) {
			((DefaultResponseHandler)responseHandler).setResponseCode(responseCode);
			return this;
		}

		public MockPayload setResponseHandler(ResponseHandler responseHandler) {
			this.responseHandler = responseHandler;
			return this;
		}

		public ResponseHandler getResponseHandler() {
			return responseHandler;
		}

		@Override
		public String getHttpEndPoint() {
			return null;
		}

		@Override
		public HttpRequestMethod getHttpRequestMethod() {
			return null;
		}

		@Override
		public String getHttpRequestContentType() {
			return null;
		}

		@Override
		public String toString() {
			return json;
		}
	}

	class MockPayloadRequestSender implements PayloadRequestSender {
		private final HttpRequestManager requestManager;

		public MockPayloadRequestSender() {
			requestManager = new HttpRequestManager(networkQueue);
		}

		@Override
		public HttpRequest sendPayload(Payload payload, HttpRequest.Listener<HttpRequest> listener) {
			MockHttpRequest request = new MockHttpRequest("http://apptentive.com");
			request.setMockResponseHandler(((MockPayload) payload).getResponseHandler());
			request.addListener(listener);
			requestManager.startRequest(request);
			return request;
		}
	}
}