/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.model.Payload;
import com.apptentive.android.sdk.notifications.ApptentiveNotification;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationObserver;
import com.apptentive.android.sdk.util.Destroyable;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.apptentive.android.sdk.ApptentiveLogTag.PAYLOADS;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_APP_ENTER_BACKGROUND;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_APP_ENTER_FOREGROUND;
import static com.apptentive.android.sdk.debug.Assert.assertNotNull;
import static com.apptentive.android.sdk.debug.Assert.assertNull;

class PayloadSender implements ApptentiveNotificationObserver, Destroyable {
	private static final long RETRY_TIMEOUT_NO_CONNECTION = 5000;
	private static final long RETRY_TIMEOUT_SERVER_ERROR = 5000;

	private final Map<Payload.BaseType, PayloadTypeSender> senderLookup;
	private final AtomicBoolean busy;

	private Listener listener;

	PayloadSender() {
		senderLookup = new HashMap<>();
		busy = new AtomicBoolean();

		ApptentiveNotificationCenter.defaultCenter().addObserver(NOTIFICATION_APP_ENTER_BACKGROUND, this);
		ApptentiveNotificationCenter.defaultCenter().addObserver(NOTIFICATION_APP_ENTER_FOREGROUND, this);
	}

	//region Payloads

	public boolean sendPayload(final Payload payload) {
		if (payload == null) {
			throw new IllegalArgumentException("Payload is null");
		}

		// we don't allow concurrent payload sending
		if (isBusy()) {
			return false;
		}

		setBusy(true);

		DispatchQueue.backgroundQueue().dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				sendPayloadSync(payload);
			}
		});

		return true;
	}

	private void sendPayloadSync(Payload payload) {
		try {
			sendPayloadSyncGuarded(payload);
		} catch (Exception e) {
			handleFailSendingPayload(payload, e.getMessage());
		}
	}

	private void sendPayloadSyncGuarded(Payload payload) {
		ApptentiveLog.d(PAYLOADS, "Sending payload: %s:%d (%s)", payload.getBaseType(), payload.getDatabaseId(), payload.getConversationId());

		PayloadTypeSender typeSender = senderLookup.get(payload.getBaseType());
		if (typeSender == null) {
			handleFailSendingPayload(payload, "Unknown payload type: %s", payload.getBaseType());
			return;
		}

		ApptentiveHttpResponse response = typeSender.sendPayload(payload);
		assertNotNull(response);

		// that should probably not happen but we still need to handle this case
		if (response == null) {
			handleFailSendingPayload(payload, "Null-response for payload: %s", payload.getBaseType());
			return;
		}

		if (response.isSuccessful()) {
			ApptentiveLog.v(PAYLOADS, "Payload submission successful: %s", payload);
			handleFinishSendingPayload(payload);
		}
		else if (response.isRejectedPermanently() || response.isBadPayload()) {
			ApptentiveLog.v(PAYLOADS, "Payload response was rejected or invalid: %s", payload);
			handleFinishSendingPayload(payload);
		}
		else if (response.isRejectedTemporarily()) {
			ApptentiveLog.v(PAYLOADS, "Payload was temporary rejected: %s");
			if (response.isException()) {
				retryPayloadSending(payload, RETRY_TIMEOUT_NO_CONNECTION);
			} else {
				retryPayloadSending(payload, RETRY_TIMEOUT_SERVER_ERROR);
			}
		} else {
			handleFailSendingPayload(payload, "Payload submission failed due to an unknown cause: %s");
		}
	}

	//endregion

	//region Sending Retry

	private void retryPayloadSending(final Payload payload, long delayMillis) {
		ApptentiveLog.v(PAYLOADS, "Retrying sending payload: %s", payload);
		setBusy(true);

		DispatchQueue.backgroundQueue().dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				sendPayload(payload);
			}
		}, delayMillis);
	}

	//endregion

	//region Listener notification

	private void handleFinishSendingPayload(Payload payload) {
		setBusy(false);

		try {
			if (listener != null) {
				listener.onFinishSending(this, payload);
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while notifying payload listener");
		}
	}

	private void handleFailSendingPayload(Payload payload, String format, Object... args) {
		setBusy(false);

		try {
			if (listener != null) {
				listener.onFailSending(this, payload, StringUtils.format(format, args));
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while notifying payload listener");
		}
	}

	//endregion

	//region Type Senders

	public void registerTypeSender(Payload.BaseType type, PayloadTypeSender sender) {
		PayloadTypeSender existing = senderLookup.put(type, sender);
		assertNull(existing, "Payload type sender already registered");
	}

	//endregion

	//region Background/Foreground

	private void onAppEnterBackground() {
	}

	private void onAppEnterForeground() {
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

	public boolean isBusy() {
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
		void onFinishSending(PayloadSender sender, Payload payload);

		void onFailSending(PayloadSender sender, Payload payload, String errorMessage);
	}

	//endregion
}
