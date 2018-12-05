/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.PayloadData;
import com.apptentive.android.sdk.network.HttpRequest;
import com.apptentive.android.sdk.network.HttpRequestRetryPolicy;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONObject;

import static com.apptentive.android.sdk.ApptentiveHelper.conversationQueue;
import static com.apptentive.android.sdk.ApptentiveLogTag.PAYLOADS;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_AUTHENTICATION_FAILED;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_AUTHENTICATION_FAILED_REASON;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_CONVERSATION_ID;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

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
	synchronized boolean sendPayload(final PayloadData payload) {
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
			ApptentiveLog.e(e, "Exception while sending payload: %s", payload);
			logException(e);

			// for NullPointerException, the message object would be null, we should handle it separately
			// TODO: add a helper class for handling that
			String message = e.getMessage();
			if (message == null) {
				message = StringUtils.format("%s is thrown", e.getClass().getSimpleName());
			}

			// if an exception was thrown - mark payload as failed
			handleFinishSendingPayload(payload, false, message, -1, null); // TODO: a better approach
		}

		return true;
	}

	/**
	 * Creates and sends payload Http-request asynchronously (returns immediately)
	 * @param payload
	 */
	private synchronized void sendPayloadRequest(final PayloadData payload) {
		ApptentiveLog.v(PAYLOADS, "Sending payload: %s", payload);

		// create request object
		final HttpRequest payloadRequest = requestSender.createPayloadSendRequest(payload, new HttpRequest.Listener<HttpRequest>() {
			@Override
			public void onFinish(HttpRequest request) {
				try {
					String json = StringUtils.isNullOrEmpty(request.getResponseData()) ? "{}" : request.getResponseData();
					final JSONObject responseData = new JSONObject(json);
					handleFinishSendingPayload(payload, false, null, request.getResponseCode(), responseData);
				} catch (Exception e) {
					// TODO: Stop assuming the response is JSON. In fact, just send bytes back, and whatever part of the SDK needs it can try to convert it to the desired format.
					ApptentiveLog.e(PAYLOADS, e, "Exception while handling payload send response");
					logException(e);

					handleFinishSendingPayload(payload, false, null, -1, null);
				}
			}

			@Override
			public void onCancel(HttpRequest request) {
				handleFinishSendingPayload(payload, true, null, request.getResponseCode(), null);
			}

			@Override
			public void onFail(HttpRequest request, String reason) {
				if (request.isAuthenticationFailure()) {
					ApptentiveNotificationCenter.defaultCenter().postNotification(NOTIFICATION_AUTHENTICATION_FAILED, NOTIFICATION_KEY_CONVERSATION_ID, payload.getConversationId(), NOTIFICATION_KEY_AUTHENTICATION_FAILED_REASON, request.getAuthenticationFailedReason());
				}
				handleFinishSendingPayload(payload, false, reason, request.getResponseCode(), null);
			}
		});

		// set 'retry' policy
		payloadRequest.setRetryPolicy(requestRetryPolicy);
		payloadRequest.setCallbackQueue(conversationQueue());
		payloadRequest.start();
	}

	//endregion

	//region Listener notification

	/**
	 * Executed when we're done with the current payload
	 * @param payload      - current payload
	 * @param cancelled    - flag indicating if payload Http-request was cancelled
	 * @param errorMessage - if not <code>null</code> - payload request failed
	 * @param responseCode - http-request response code
	 * @param responseData - http-reqeust response json (or null if failed)
	 */
	private synchronized void handleFinishSendingPayload(PayloadData payload, boolean cancelled, String errorMessage, int responseCode, JSONObject responseData) {
		sendingFlag = false; // mark sender as 'not busy'

		try {
			if (listener != null) {
				listener.onFinishSending(this, payload, cancelled, errorMessage, responseCode, responseData);
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while notifying payload listener");
			logException(e);
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
		void onFinishSending(PayloadSender sender, PayloadData payload, boolean cancelled, String errorMessage, int responseCode, JSONObject responseData);
	}

	//endregion
}
