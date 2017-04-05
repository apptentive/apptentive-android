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
import com.apptentive.android.sdk.notifications.ApptentiveNotification;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationObserver;
import com.apptentive.android.sdk.util.Destroyable;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.apptentive.android.sdk.ApptentiveLogTag.PAYLOADS;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_APP_ENTER_BACKGROUND;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_APP_ENTER_FOREGROUND;

class PayloadSender implements ApptentiveNotificationObserver, Destroyable {
	private static final long RETRY_TIMEOUT = 5000;
	private static final int RETRY_MAX_COUNT = 5;

	private final PayloadRequestSender requestSender;
	private final HttpRequestRetryPolicy requestRetryPolicy;
	private final AtomicBoolean busy;

	private Listener listener;
	private boolean appInBackground;

	PayloadSender(PayloadRequestSender requestSender) {
		if (requestSender == null) {
			throw new IllegalArgumentException("Payload request sender is null");
		}

		this.requestSender = requestSender;

		requestRetryPolicy = new HttpRequestRetryPolicy() {
			@Override
			protected boolean shouldRetryRequest(int responseCode) {
				return !(appInBackground || responseCode >= 400 && responseCode < 500);

			}
		};
		requestRetryPolicy.setRetryTimeoutMillis(RETRY_TIMEOUT);
		requestRetryPolicy.setMaxRetryCount(RETRY_MAX_COUNT);

		busy = new AtomicBoolean();

		ApptentiveNotificationCenter.defaultCenter().addObserver(NOTIFICATION_APP_ENTER_BACKGROUND, this);
		ApptentiveNotificationCenter.defaultCenter().addObserver(NOTIFICATION_APP_ENTER_FOREGROUND, this);
	}

	//region Payloads

	boolean sendPayload(final Payload payload) {
		if (payload == null) {
			throw new IllegalArgumentException("Payload is null");
		}

		// we don't allow concurrent payload sending
		if (isBusy()) {
			return false;
		}

		setBusy(true);

		try {
			sendPayloadRequest(payload);
		} catch (Exception e) {
			handleFinishSendingPayload(payload, false, e.getMessage());
		}

		return true;
	}

	private void sendPayloadRequest(final Payload payload) {
		ApptentiveLog.d(PAYLOADS, "Sending payload: %s:%d (%s)", payload.getBaseType(), payload.getDatabaseId(), payload.getConversationId());

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

	private void handleFinishSendingPayload(Payload payload, boolean cancelled, String errorMessage) {
		setBusy(false);

		try {
			if (listener != null) {
				listener.onFinishSending(this, payload, cancelled, errorMessage);
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while notifying payload listener");
		}
	}

	//endregion

	//region Background/Foreground

	private void onAppEnterBackground() {
		appInBackground = true;
	}

	private void onAppEnterForeground() {
		appInBackground = false;
	}

	//endregion

	//region Destroyable

	@Override
	public void destroy() {
		ApptentiveNotificationCenter.defaultCenter().removeObserver(this);
	}

	//endregion

	//region ApptentiveNotificationObserver

	@Override
	public void onReceiveNotification(ApptentiveNotification notification) {
		if (notification.hasName(NOTIFICATION_APP_ENTER_BACKGROUND)) {
			onAppEnterBackground();
		} else if (notification.hasName(NOTIFICATION_APP_ENTER_FOREGROUND)) {
			onAppEnterForeground();
		}
	}

	//endregion

	//region Getters/Setters

	boolean isBusy() {
		return busy.get();
	}

	private void setBusy(boolean value) {
		busy.set(value);
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
