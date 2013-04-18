/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.offline.SurveyPayload;

/**
 * @author Sky Kelsey
 */
public class PayloadSendWorker {

	private static final int NO_TOKEN_SLEEP = 5000;
	private static final int EMPTY_QUEUE_SLEEP_TIME = 5000;

	private static boolean running;

	public static synchronized void start() {
		if (!running) {
			Log.i("Starting PayloadRunner.");
			running = true;
			new PayloadRunner().start();
		}
	}

	private static PayloadStore getPayloadStore() {
		return Apptentive.getDatabase();
	}

	private static class PayloadRunner extends Thread {
		public void run() {
			try {
				synchronized (this) {
					PayloadStore db = getPayloadStore();
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
								response = ApptentiveClient.postMessage((Message) payload);
								MessageManager.onSentMessage((Message) payload, response);
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
							case survey:
								response = ApptentiveClient.postSurvey((SurveyPayload) payload);
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
