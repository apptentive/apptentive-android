/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.Payload;
import com.apptentive.android.sdk.network.HttpJsonRequest;
import com.apptentive.android.sdk.network.HttpRequest;
import com.apptentive.android.sdk.network.HttpRequestRetryPolicy;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;

import static com.apptentive.android.sdk.ApptentiveLogTag.PAYLOADS;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_PAYLOAD;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_SUCCESSFUL;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_PAYLOAD_DID_SEND;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_PAYLOAD_WILL_SEND;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

class PayloadSender {
	private static final long RETRY_TIMEOUT = 5000;
	private static final int RETRY_MAX_COUNT = 5;

	private final PayloadRequestSender requestSender;
	private final HttpRequestRetryPolicy requestRetryPolicy;

	private Listener listener;
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

	synchronized boolean sendPayload(final Payload payload) {
		if (payload == null) {
			throw new IllegalArgumentException("Payload is null");
		}

		// we don't allow concurrent payload sending
		if (isSendingPayload()) {
			return false;
		}

		sendingFlag = true;

		try {
			sendPayloadRequest(payload);
		} catch (Exception e) {
			handleFinishSendingPayload(payload, false, e.getMessage());
		}

		return true;
	}

	private synchronized void sendPayloadRequest(final Payload payload) {
		ApptentiveLog.d(PAYLOADS, "Sending payload: %s:%d (%s)", payload.getBaseType(), payload.getDatabaseId(), payload.getConversationId());

		ApptentiveNotificationCenter.defaultCenter()
			.postNotification(NOTIFICATION_PAYLOAD_WILL_SEND, NOTIFICATION_KEY_PAYLOAD, payload);

		final HttpRequest payloadRequest = requestSender.sendPayload(payload, new HttpRequest.Listener<HttpJsonRequest>() {
			@Override
			public void onFinish(HttpJsonRequest request) {
				handleFinishSendingPayload(payload, false, null);
			}

			@Override
			public void onCancel(HttpJsonRequest request) {
				handleFinishSendingPayload(payload, true, null);
			}

			@Override
			public void onFail(HttpJsonRequest request, String reason) {
				handleFinishSendingPayload(payload, false, reason);
			}
		});
		payloadRequest.setRetryPolicy(requestRetryPolicy);
	}

	//endregion

	//region Listener notification

	private synchronized void handleFinishSendingPayload(Payload payload, boolean cancelled, String errorMessage) {
		sendingFlag = false;

		ApptentiveNotificationCenter.defaultCenter()
			.postNotification(NOTIFICATION_PAYLOAD_DID_SEND,
				NOTIFICATION_KEY_PAYLOAD, payload,
				NOTIFICATION_KEY_SUCCESSFUL, errorMessage == null ? TRUE : FALSE);

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

	synchronized boolean isSendingPayload() {
		return sendingFlag;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	//endregion

	//region Listener

	public interface Listener {
		void onFinishSending(PayloadSender sender, Payload payload, boolean cancelled, String errorMessage);
	}

	//endregion
}
