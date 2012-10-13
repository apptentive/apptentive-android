/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.offline;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;

/**
 * @author Sky Kelsey
 */
public class PayloadManager {

	private static final int WAIT_TIMEOUT_SECONDS = 5;

	private static boolean running;

	public static synchronized void start() {
		if (!running) {
			Log.i("Starting PayloadRunner.");
			running = true;
			new PayloadRunner().start();
		}
	}

	public static synchronized void putPayload(Payload payload) {
		getPayloadStore().addPayload(payload);
		start();
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
						Payload payload = null;
						payload = db.getNextPayload();
						if (payload == null) {
							// There is no payload in the db.
							try {
								Thread.sleep(WAIT_TIMEOUT_SECONDS * 1000);
							} catch (InterruptedException e) {
							}
							continue;
						}
						Log.d("Got a payload to send: " + payload.getPayloadId());
						String json = payload.toString();
						Log.v("Payload contents: " + json);
						ApptentiveHttpResponse response = null;
						switch (payload.getPayloadType()) {
							case RECORD:
								response = ApptentiveClient.postRecord(json);
								break;
							case MESSAGE:
								response = ApptentiveClient.postMessage(json);
								MessageManager.sentMessage(payload.getPayloadId(), response);
								break;
							default:
								break;
						}

						if (response != null && response.wasSuccessful()) { // Success
							db.deletePayload(payload);
						} else if (response.getCode() >= 400 && response.getCode() < 500) { // Rejected by server.
							Log.e("Payload %s rejected.", payload.getPayloadId());
							db.deletePayload(payload);
						} else { // Transient error / overload.
							Log.d("Unable to send JSON. Leaving in queue.");
							// Break the loop. Restart when network is reachable.
							break;
						}
					}
				}
			} finally {
				running = false;
			}
		}
	}
}
