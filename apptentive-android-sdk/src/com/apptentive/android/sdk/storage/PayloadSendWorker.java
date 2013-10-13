/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
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

/**
 * @author Sky Kelsey
 */
public class PayloadSendWorker {

	private static final int NO_TOKEN_SLEEP = 5000;
	private static final int EMPTY_QUEUE_SLEEP_TIME = 5000;

	private static boolean running;
	private static Context appContext;

	public static synchronized void start(Context context) {
		appContext = context.getApplicationContext();
		if (!running) {
			Log.i("Starting PayloadRunner.");
			running = true;
			new PayloadRunner().start();
		}
	}

	private static PayloadStore getPayloadStore(Context context) {
		return ApptentiveDatabase.getInstance(context);
	}

	private static class PayloadRunner extends Thread {
		public void run() {
			try {
				synchronized (this) {
					if(appContext == null) {
						return;
					}
					PayloadStore db = getPayloadStore(appContext);
					while (true) {
						if (GlobalInfo.conversationToken == null || GlobalInfo.conversationToken.equals("")) {
							pause(NO_TOKEN_SLEEP);
							continue;
						}
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
							} else if (response.isRejectedPermanently() || response.isBadpayload()) {
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
				running = false;
			}
		}
	}

	private static void pause(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}
}
