/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
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
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey
 */
public class PayloadSendWorker {

	private static final int NO_TOKEN_SLEEP = 5000;
	private static final int EMPTY_QUEUE_SLEEP_TIME = 5000;
	private static final int NO_CONNECTION_SLEEP_TIME = 5000;

	private static boolean appInForeground;
	private static boolean threadRunning;
	private static Context appContext;
	private static PayloadSendThread payloadSendThread;

	public static synchronized void doStart(Context context) {
		appContext = context.getApplicationContext();
		if (!threadRunning) {
			Log.i("Starting PayloadSendWorker.");
			threadRunning = true;
			payloadSendThread = new PayloadSendThread();
			Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread thread, Throwable throwable) {
					Log.e("Error in PayloadSendWorker.", throwable);
					MetricModule.sendError(appContext, throwable, null, null);
				}
			};
			payloadSendThread.setUncaughtExceptionHandler(handler);
			payloadSendThread.setName("Apptentive-PayloadSendWorker");
			payloadSendThread.start();
		}
	}

	private static PayloadStore getPayloadStore(Context context) {
		return ApptentiveDatabase.getInstance(context);
	}

	private static class PayloadSendThread extends Thread {
		public void run() {
			try {
				synchronized (this) {
					Log.v("Started %s", toString());
					if (appContext == null) {
						return;
					}
					PayloadStore db = getPayloadStore(appContext);
					while (appInForeground) {
						if (Util.isEmpty(GlobalInfo.conversationToken)) {
							Log.i("No conversation token yet.");
							goToSleep(NO_TOKEN_SLEEP);
							continue;
						}
						if (!Util.isNetworkConnectionPresent(appContext)) {
							Log.d("Can't send payloads. No network connection.");
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
								response = ApptentiveClient.postMessage(appContext, (Message) payload);
								MessageManager.onSentMessage(appContext, (Message) payload, response);
								break;
							case event:
								response = ApptentiveClient.postEvent((Event) payload);
								break;
							case device:
								response = ApptentiveClient.putDevice((Device) payload);
								DeviceManager.onSentDeviceInfo(appContext);
								break;
							case sdk:
								response = ApptentiveClient.putSdk((Sdk) payload);
								break;
							case app_release:
								response = ApptentiveClient.putAppRelease((AppRelease) payload);
								break;
							case person:
								response = ApptentiveClient.putPerson((Person) payload);
								break;
							case survey:
								response = ApptentiveClient.postSurvey((SurveyResponse) payload);
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
								continue;
							}
						}
					}
				}
			} finally {
				Log.v("Stopping PayloadSendThread.");
				threadRunning = false;
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
		if (payloadSendThread != null) {
			Log.v("Waking PayloadSendThread.");
			payloadSendThread.interrupt();
		}
	}

	public static void appWentToForeground(Context context) {
		appInForeground = true;
		doStart(context);
	}

	public static void appWentToBackground() {
		appInForeground = false;
		wakeUp();
	}
}
