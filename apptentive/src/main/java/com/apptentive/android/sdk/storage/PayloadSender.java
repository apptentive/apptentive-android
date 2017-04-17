/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.JsonPayload;
import com.apptentive.android.sdk.network.HttpRequest;
import com.apptentive.android.sdk.network.HttpRequestRetryPolicy;
import com.apptentive.android.sdk.util.StringUtils;

import static com.apptentive.android.sdk.ApptentiveLogTag.PAYLOADS;

/**
 * Class responsible for a serial payload sending (one at a time)
 */
class PayloadSender {
	/**
	 * Object which creates and send Http-request for payloads
	 */
	private final PayloadRequestSender requestSender;

	/**
	 * Payload Http-request retry policy
	 */
	private final HttpRequestRetryPolicy requestRetryPolicy;

	private Listener listener;

	/**
	 * Indicates whenever the sender is busy sending a payload
	 */
	private boolean sendingFlag; // this variable is only accessed in a synchronized context

	PayloadSender(PayloadRequestSender requestSender, HttpRequestRetryPolicy retryPolicy) {
		if (requestSender == null) {
			throw new IllegalArgumentException("Payload request sender is null");
		}

		if (retryPolicy == null) {
			throw new IllegalArgumentException("Retry policy is null");
		}

		this.requestSender = requestSender;
		this.requestRetryPolicy = retryPolicy;
	}

	//region Payloads

	/**
	 * Sends payload asynchronously. Returns boolean flag immediately indicating if payload send was
	 * scheduled
	 *
	 * @throws IllegalArgumentException is payload is null
	 */
	synchronized boolean sendPayload(final JsonPayload payload) {
		if (payload == null) {
			throw new IllegalArgumentException("Payload is null");
		}

		// we don't allow concurrent payload sending
		if (isSendingPayload()) {
			return false;
		}

		// we mark the sender as "busy" so no other payloads would be sent until we're done
		sendingFlag = true;

		try {
			sendPayloadRequest(payload);
		} catch (Exception e) {
			ApptentiveLog.e(PAYLOADS, "Exception while sending payload: %s", payload);

			// for NullPointerException, the message object would be null, we should handle it separately
			// TODO: add a helper class for handling that
			String message = e.getMessage();
			if (message == null) {
				message = StringUtils.format("%s is thrown", e.getClass().getSimpleName());
			}

			// if an exception was thrown - mark payload as failed
			handleFinishSendingPayload(payload, false, message);
		}

		return true;
	}

	/**
	 * Creates and sends payload Http-request asynchronously (returns immediately)
	 */
	private synchronized void sendPayloadRequest(final JsonPayload payload) {
		ApptentiveLog.d(PAYLOADS, "Sending payload: %s:%d (%s)", payload.getBaseType(), payload.getDatabaseId(), payload.getConversationId());

		// create request object
		final HttpRequest payloadRequest = requestSender.sendPayload(payload, new HttpRequest.Listener<HttpRequest>() {
			@Override
			public void onFinish(HttpRequest request) {
				handleFinishSendingPayload(payload, false, null);
			}

			@Override
			public void onCancel(HttpRequest request) {
				handleFinishSendingPayload(payload, true, null);
			}

			@Override
			public void onFail(HttpRequest request, String reason) {
				handleFinishSendingPayload(payload, false, reason);
			}
		});

		// set 'retry' policy
		payloadRequest.setRetryPolicy(requestRetryPolicy);
	}

	//endregion

	//region Listener notification

	/**
	 * Executed when we're done with the current payload
	 *
	 * @param payload      - current payload
	 * @param cancelled    - flag indicating if payload Http-request was cancelled
	 * @param errorMessage - if not <code>null</code> - payload request failed
	 */
	private synchronized void handleFinishSendingPayload(JsonPayload payload, boolean cancelled, String errorMessage) {
		sendingFlag = false; // mark sender as 'not busy'

		try {
			if (listener != null) {
				listener.onFinishSending(this, payload, cancelled, errorMessage);
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while notifying payload listener");
		}
	}

	//endregion

	//region Getters/Setters

	/**
	 * Returns <code>true</code> if sender is currently busy with a payload
	 */
	synchronized boolean isSendingPayload() {
		return sendingFlag;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	//endregion

	//region Listener

	public interface Listener {
		void onFinishSending(PayloadSender sender, JsonPayload payload, boolean cancelled, String errorMessage);
	}

	//endregion
}
