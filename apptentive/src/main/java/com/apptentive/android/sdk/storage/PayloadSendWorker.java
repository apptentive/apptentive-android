/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sky Kelsey
 */
public class PayloadSendWorker {

	private static final int NO_TOKEN_SLEEP = 5000;
	private static final int NO_CONNECTION_SLEEP_TIME = 5000;
	private static final int SERVER_ERROR_SLEEP_TIME = 5000;

	private static final int UI_THREAD_MESSAGE_RETRY = 1;

	private PayloadSendRunnable payloadSendRunnable;
	private Handler uiHandler;

	private AtomicBoolean appInForeground = new AtomicBoolean(false);
	private AtomicBoolean threadRunning = new AtomicBoolean(false);
	private AtomicBoolean threadCanRun = new AtomicBoolean(false);


	public PayloadSendWorker() {
	}

	public synchronized  void runPayloadSendRunnable(boolean expect,
																																								boolean createNew) {
		if (expect && createNew && payloadSendRunnable == null) {
			payloadSendRunnable = new PayloadSendRunnable();
		} else if (!expect) {
			payloadSendRunnable = null;
			uiHandler = null;
		}

		if (payloadSendRunnable != null) {
			if (uiHandler == null) {
				uiHandler = new Handler(Looper.getMainLooper()) {
					@Override
					public void handleMessage(android.os.Message msg) {
						switch (msg.what) {
							case UI_THREAD_MESSAGE_RETRY:
								runPayloadSendRunnable(true, true);
								break;
							default:
								super.handleMessage(msg);
						}
					}
				};
			}
			if (threadCanRun.get() && !threadRunning.get()) {
				threadRunning.set(true);
				ApptentiveInternal.getInstance().runOnWorkerThread(payloadSendRunnable);
			}
		}
	}

	private PayloadStore getPayloadStore() {
		return ApptentiveInternal.getInstance().getApptentiveDatabase();
	}

	private class PayloadSendRunnable implements Runnable {

		public PayloadSendRunnable() {
		}

		public void run() {
			try {
				ApptentiveLog.v("Started %s", toString());

				while (appInForeground.get()) {
					MessageManager mgr = ApptentiveInternal.getInstance().getMessageManager();

					PayloadStore db = getPayloadStore();
					if (TextUtils.isEmpty(ApptentiveInternal.getInstance().getApptentiveConversationToken())){
						ApptentiveLog.i("No conversation token yet.");
						if (mgr != null) {
							mgr.onPauseSending(MessageManager.SEND_PAUSE_REASON_SERVER);
						}
						retryLater(NO_TOKEN_SLEEP);
						break;
					}
					if (!Util.isNetworkConnectionPresent()) {
						ApptentiveLog.d("Can't send payloads. No network connection.");
						if (mgr != null) {
							mgr.onPauseSending(MessageManager.SEND_PAUSE_REASON_NETWORK);
						}
						retryLater(NO_CONNECTION_SLEEP_TIME);
						break;
					}
					ApptentiveLog.v("Checking for payloads to send.");
					Payload payload;
					payload = db.getOldestUnsentPayload();
					if (payload == null) {
						// There is no payload in the db. Terminate the thread
						threadCanRun.set(false);
						break;
					}
					ApptentiveLog.d("Got a payload to send: %s:%d", payload.getBaseType(), payload.getDatabaseId());

					ApptentiveHttpResponse response = null;


					switch (payload.getBaseType()) {
						case message:
							if (mgr != null) {
								mgr.onResumeSending();
							}
							response = ApptentiveClient.postMessage((ApptentiveMessage) payload);
							if (mgr != null) {
								mgr.onSentMessage((ApptentiveMessage) payload, response);
							}
							break;
						case event:
							response = ApptentiveClient.postEvent((Event) payload);
							break;
						case device:
							response = ApptentiveClient.putDevice((Device) payload);
							DeviceManager.onSentDeviceInfo();
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
							ApptentiveLog.e("Didn't send unknown Payload BaseType: " + payload.getBaseType());
							db.deletePayload(payload);
							break;
					}

					// Each Payload type is handled by the appropriate handler, but if sent correctly, or failed permanently to send, it should be removed from the queue.
					if (response != null) {
						if (response.isSuccessful()) {
							ApptentiveLog.d("Payload submission successful. Removing from send queue.");
							db.deletePayload(payload);
						} else if (response.isRejectedPermanently() || response.isBadPayload()) {
							ApptentiveLog.d("Payload rejected. Removing from send queue.");
							ApptentiveLog.v("Rejected json:", payload.toString());
							db.deletePayload(payload);
						} else if (response.isRejectedTemporarily()) {
							ApptentiveLog.d("Unable to send JSON. Leaving in queue.");
							if (response.isException()) {
								if (mgr != null) {
									mgr.onPauseSending(MessageManager.SEND_PAUSE_REASON_SERVER);
								}
								retryLater(NO_CONNECTION_SLEEP_TIME);
								break;
							} else {
								retryLater(SERVER_ERROR_SLEEP_TIME);
								break;
							}
						}
					}
				}
			} catch (Throwable throwable) {
				MetricModule.sendError(throwable, null, null);
			} finally
			{
				ApptentiveLog.v("Stopping PayloadSendThread.");
				threadRunning.set(false);
			}
		}

		private void retryLater(int millis) {
			Message msg = uiHandler.obtainMessage(UI_THREAD_MESSAGE_RETRY);
			uiHandler.removeMessages(UI_THREAD_MESSAGE_RETRY);
			uiHandler.sendMessageDelayed(msg, millis);
		}
	}

	public void appWentToForeground() {
		appInForeground.set(true);
		runPayloadSendRunnable(true, true);
	}

	public void appWentToBackground() {
		appInForeground.set(false);
		runPayloadSendRunnable(true, false);
	}

	public void setCanRunPayloadThread(boolean b) {
		threadCanRun.set(b);
		runPayloadSendRunnable(true, true);
	}
}
