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
						ActivityFeedItem activityFeedItem = null;
						activityFeedItem = db.getOldestUnsentRecord();
						if (activityFeedItem == null) {
							// There is no payload in the db.
							pause(EMPTY_QUEUE_SLEEP_TIME);
							continue;
						}
						Log.d("Got a payload to send: " + activityFeedItem.getNonce());
						// Wrap the record in a JSONObject.
						JSONObject wrapper = new JSONObject();
						try {
							wrapper.put(ActivityFeedItem.BaseType.message.name(), activityFeedItem);
						} catch (JSONException e) {
							Log.w("Error wrapping Record in JSONObject.", e);
							db.deleteRecord(activityFeedItem);
							continue;
						}
						String json = wrapper.toString();
						Log.d("Payload contents: " + json);

						ApptentiveHttpResponse response = null;

						switch (activityFeedItem.getBaseType()) {
							case message:
								response = ApptentiveClient.postMessage(json);
								MessageManager.onSentMessage((Message) activityFeedItem, response);
								break;
							case event:
								response = ApptentiveClient.postEvent(json);
								EventManager.onSentEvent((Event) activityFeedItem, response);
								break;
							default:
								Log.d("Sent unknown ActivityFeedItemType: " + activityFeedItem.getType());
								break;
						}

						// Each Record type is handled by the appropriate handler, but if the message send fails permanently, delete it.
						if (response != null) {
							if (response.wasSuccessful()) {
								Log.d("ActivityFeedItem submission successful. Marking sent.", activityFeedItem.getNonce());
								activityFeedItem.setState(ActivityFeedItem.State.sent);
								db.updateRecord(activityFeedItem);
							} else if (response.wasRejectedPermanently()) {
								Log.d("ActivityFeedItem %s rejected.", activityFeedItem.getNonce());
								Log.v("Rejected json:", activityFeedItem.toString());
								db.deleteRecord(activityFeedItem);
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
