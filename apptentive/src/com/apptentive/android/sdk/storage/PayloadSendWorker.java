/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Util;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sky Kelsey
 */
public class PayloadSendWorker {

	private static final int NO_TOKEN_SLEEP = 5000;
	private static final int EMPTY_QUEUE_SLEEP_TIME = 5000;
	private static final int NO_CONNECTION_SLEEP_TIME = 5000;
	private static final int SERVER_ERROR_SLEEP_TIME = 5000;

	private static PayloadSendThread sPayloadSendThread;

	private static AtomicBoolean appInForeground = new AtomicBoolean(false);
	private static AtomicBoolean threadRunning = new AtomicBoolean(false);

	// A synchronized getter/setter to the static instance of thread object
	public static synchronized PayloadSendThread getAndSetPayloadSendThread(boolean expect,
																																								boolean createNew,
																																								Context context) {
		if (expect && createNew && context != null) {
			sPayloadSendThread = createPayloadSendThread(context.getApplicationContext());
		} else if (!expect) {
			sPayloadSendThread = null;
		}
		return  sPayloadSendThread;
	}


	private static PayloadSendThread createPayloadSendThread(final Context appContext) {
		PayloadSendThread newThread = new PayloadSendThread(appContext);
		Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
				MetricModule.sendError(appContext, throwable, null, null);
			}
		};
		newThread.setUncaughtExceptionHandler(handler);
		newThread.setName("Apptentive-PayloadSendWorker");
		newThread.start();
		return newThread;
	}

	private static PayloadStore getPayloadStore(Context context) {
		return ApptentiveDatabase.getInstance(context);
	}

	private static class PayloadSendThread extends Thread {
		private WeakReference<Context> contextRef;

		public PayloadSendThread(Context appContext) {
			contextRef = new WeakReference<>(appContext);
		}

		public void run() {
			try {
				Log.v("Started %s", toString());

				while (appInForeground.get()) {
					if (contextRef.get() == null) {
						threadRunning.set(false);
						return;
					}

					PayloadSendThread thread = getAndSetPayloadSendThread(true, false, null);
					if (thread != null && thread != PayloadSendThread.this) {
						Log.i("something wrong");
						return;
					}

					PayloadStore db = getPayloadStore(contextRef.get());
					if (Util.isEmpty(GlobalInfo.conversationToken)) {
						Log.i("No conversation token yet.");
						MessageManager.onPauseSending(MessageManager.SEND_PAUSE_REASON_SERVER);
						goToSleep(NO_TOKEN_SLEEP);
						continue;
					}
					if (!Util.isNetworkConnectionPresent(contextRef.get())) {
						Log.d("Can't send payloads. No network connection.");
						MessageManager.onPauseSending(MessageManager.SEND_PAUSE_REASON_NETWORK);
						goToSleep(NO_CONNECTION_SLEEP_TIME);
						continue;
					}
					Log.v("Checking for payloads to send.");
					Payload payload;
					payload = db.getOldestUnsentPayload();
					if (payload == null) {
						// There is no payload in the db.
						goToSleep(EMPTY_QUEUE_SLEEP_TIME);
						continue;
					}
					Log.d("Got a payload to send: %s:%d", payload.getBaseType(), payload.getDatabaseId());

					ApptentiveHttpResponse response = null;


					switch (payload.getBaseType()) {
						case message:
							MessageManager.onResumeSending();
							response = ApptentiveClient.postMessage(contextRef.get(), (ApptentiveMessage) payload);
							MessageManager.onSentMessage(contextRef.get(), (ApptentiveMessage) payload, response);
							break;
						case event:
							response = ApptentiveClient.postEvent(contextRef.get(), (Event) payload);
							break;
						case device:
							response = ApptentiveClient.putDevice(contextRef.get(), (Device) payload);
							DeviceManager.onSentDeviceInfo(contextRef.get());
							break;
						case sdk:
							response = ApptentiveClient.putSdk(contextRef.get(), (Sdk) payload);
							break;
						case app_release:
							response = ApptentiveClient.putAppRelease(contextRef.get(), (AppRelease) payload);
							break;
						case person:
							response = ApptentiveClient.putPerson(contextRef.get(), (Person) payload);
							break;
						case survey:
							response = ApptentiveClient.postSurvey(contextRef.get(), (SurveyResponse) payload);
							break;
						default:
							Log.e("Didn't send unknown Payload BaseType: " + payload.getBaseType());
							db.deletePayload(payload);
							break;
					}

					// Each Payload type is handled by the appropriate handler, but if sent correctly, or failed permanently to send, it should be removed from the queue.
					if (response != null) {
						if (response.isSuccessful()) {
							Log.d("Payload submission successful. Removing from send queue.");
							db.deletePayload(payload);
						} else if (response.isRejectedPermanently() || response.isBadPayload()) {
							Log.d("Payload rejected. Removing from send queue.");
							Log.v("Rejected json:", payload.toString());
							db.deletePayload(payload);
						} else if (response.isRejectedTemporarily()) {
							Log.d("Unable to send JSON. Leaving in queue.");
							if (response.isException()) {
								MessageManager.onPauseSending(MessageManager.SEND_PAUSE_REASON_SERVER);
								goToSleep(NO_CONNECTION_SLEEP_TIME);
							} else {
								goToSleep(SERVER_ERROR_SLEEP_TIME);
							}
						}
					}
				}
			} finally {
				Log.v("Stopping PayloadSendThread.");
				threadRunning.set(false);
			}
		}
	}

	private static void goToSleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// Happens during normal operation.
		}
	}

	private static void wakeUp() {
		PayloadSendThread thread = getAndSetPayloadSendThread(true, false, null);
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}
	}

	public static void appWentToForeground(Context context) {
		appInForeground.set(true);
		if (threadRunning.compareAndSet(false, true)) {
			/* appInForeground was "false", and set to "true"
			*  thread was not running, and set to be running
			*/
			getAndSetPayloadSendThread(true, true, context);
		} else {
			wakeUp();
		}
	}

	public static void appWentToBackground() {
		appInForeground.set(false);
		wakeUp();
	}
}
