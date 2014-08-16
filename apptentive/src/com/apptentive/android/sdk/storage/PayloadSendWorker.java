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
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.model.SurveyResponse;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey
 */
public class PayloadSendWorker {

	private static final int NO_TOKEN_SLEEP = 5000;
	private static final int EMPTY_QUEUE_SLEEP_TIME = 5000;

	private static boolean running;
	private static Context appContext;

	public static synchronized void doStart(Context context) {
		appContext = context.getApplicationContext();
		if (!running) {
			Log.i("Starting PayloadSendWorker.");
			running = true;
			Thread payloadRunner = new PayloadRunner();
			Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread thread, Throwable throwable) {
					Log.e("Error in PayloadSendWorker.", throwable);
					MetricModule.sendError(appContext, throwable, null, null);
				}
			};
			payloadRunner.setUncaughtExceptionHandler(handler);
			payloadRunner.setName("Apptentive-PayloadSendWorker");
			payloadRunner.start();
		}
	}

	private static PayloadStore getPayloadStore(Context context) {
		return ApptentiveDatabase.getInstance(context);
	}

	private static class PayloadRunner extends Thread {
		public void run() {
			try {
				synchronized (this) {
					Log.v("Started %s", toString());
					if(appContext == null) {
						return;
					}
					PayloadStore db = getPayloadStore(appContext);
					while (runningActivities > 0) {
						if (Util.isEmpty(GlobalInfo.conversationToken)) {
							pause(NO_TOKEN_SLEEP);
							continue;
						}
						if (!Util.isNetworkConnectionPresent(appContext)) {
							break;
						}
						Log.v("Checking for payloads to send.");
						Payload payload;
						payload = db.getOldestUnsentPayload();
						if (payload == null) {
							// There is no payload in the db.
							pause(EMPTY_QUEUE_SLEEP_TIME);
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
								// Break the loop. Restart when network is reachable.
								break;
							}
						}
					}
				}
			} finally {
				Log.v("Stopping PayloadSendWorker.");
				running = false;
			}
		}
	}

	private static void pause(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// Happens during normal operation.
		}
	}

	private static long runningActivities = 0;

	public static void ensureRunning(Context context) {
		doStart(context);
	}

	public static void activityStarted(Context context) {
		runningActivities++;
		doStart(context);
	}

	public static void activityStopped() {
		runningActivities--;
		if (runningActivities < 0) {
			Log.w("PayloadSendWorker: Incorrect number of running Activities encountered. Resetting to 0.");
			runningActivities = 0;
		}
	}
}
