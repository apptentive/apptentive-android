/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.offline;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.metric.Event;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class RecordSendWorker {

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

	private static RecordStore getRecordStore() {
		return Apptentive.getDatabase();
	}

	private static class PayloadRunner extends Thread {
		public void run() {
			try {
				synchronized (this) {
					RecordStore db = getRecordStore();
					while (true) {
						if (GlobalInfo.activityFeedToken == null || GlobalInfo.activityFeedToken.equals("")) {
							pause(NO_TOKEN_SLEEP);
							continue;
						}
						ActivityFeedItem item = null;
						item = db.getOldestUnsentRecord();
						if (item == null) {
							// There is no payload in the db.
							pause(EMPTY_QUEUE_SLEEP_TIME);
							continue;
						}
						Log.d("Got a payload to send: " + item.getNonce());

						ApptentiveHttpResponse response = null;

						switch (item.getBaseType()) {
							case message:
								response = ApptentiveClient.postMessage((Message) item);
								MessageManager.onSentMessage((Message) item, response);
								break;
							case event:
								// TODO: Delete events from our DB once they are sent? THey are no longer needed.
								response = ApptentiveClient.postEvent((Event) item);
								EventManager.onSentEvent((Event) item, response);
								break;
							case survey:
								response = ApptentiveClient.postSurvey((SurveyPayload) item);
								// Survey responses don't need to be stored locally.
								if(response.wasSuccessful()) {
									db.deleteRecord(item);
								}
								break;
							default:
								Log.e("Didn't send unknown ActivityFeedItemType: " + item.getType());
								// TODO: Still send this stuff?
								break;
						}

						// Each Record type is handled by the appropriate handler, but if the message send fails permanently, delete it.
						if (response != null) {
							if (response.wasSuccessful()) {
								Log.d("ActivityFeedItem submission successful. Marking sent.", item.getNonce());
								item.setState(ActivityFeedItem.State.sent);
								db.updateRecord(item);
							} else if (response.wasRejectedPermanently()) {
								Log.d("ActivityFeedItem %s rejected.", item.getNonce());
								Log.v("Rejected json:", item.toString());
								db.deleteRecord(item);
							} else if (response.wasRejectedTemporarily()) {
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
