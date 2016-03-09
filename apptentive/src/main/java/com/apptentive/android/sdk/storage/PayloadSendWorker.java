/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.apptentive.android.sdk.ApptentiveInternal;
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
																																								boolean createNew,
																																								Context context) {
		if (expect && createNew && context != null && payloadSendRunnable == null) {
			payloadSendRunnable = new PayloadSendRunnable(context);
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
								runPayloadSendRunnable(true, true, (Context) msg.obj);
								break;
							default:
								super.handleMessage(msg);
						}
					}
				};
			}
			if (threadCanRun.get() && !threadRunning.get()) {
				threadRunning.set(true);
				ApptentiveInternal.getInstance(context).runOnWorkerThread(payloadSendRunnable);
			}
		}
	}

	private PayloadStore getPayloadStore(Context context) {
		return ApptentiveInternal.getApptentiveDatabase(context);
	}

	private class PayloadSendRunnable implements Runnable {
		private WeakReference<Context> contextRef;

		public PayloadSendRunnable(Context appContext) {
			contextRef = new WeakReference<Context>(appContext);
		}

		public void run() {
			try {
				Log.v("Started %s", toString());

				while (appInForeground.get()) {
					if (contextRef.get() == null) {
						threadRunning.set(false);
						return;
					}
					MessageManager mgr = ApptentiveInternal.getMessageManager(contextRef.get());


					PayloadStore db = getPayloadStore(contextRef.get());
					if (TextUtils.isEmpty(ApptentiveInternal.getApptentiveConversationToken(contextRef.get()))){
						Log.i("No conversation token yet.");
						if (mgr != null) {
							mgr.onPauseSending(MessageManager.SEND_PAUSE_REASON_SERVER);
						}
						retryLater(NO_TOKEN_SLEEP, contextRef.get());
						break;
					}
					if (!Util.isNetworkConnectionPresent(contextRef.get())) {
						Log.d("Can't send payloads. No network connection.");
						if (mgr != null) {
							mgr.onPauseSending(MessageManager.SEND_PAUSE_REASON_NETWORK);
						}
						retryLater(NO_CONNECTION_SLEEP_TIME, contextRef.get());
						break;
					}
					Log.v("Checking for payloads to send.");
					Payload payload;
					payload = db.getOldestUnsentPayload(contextRef.get());
					if (payload == null) {
						// There is no payload in the db. Terminate the thread
						threadCanRun.set(false);
						break;
					}
					Log.d("Got a payload to send: %s:%d", payload.getBaseType(), payload.getDatabaseId());

					ApptentiveHttpResponse response = null;


					switch (payload.getBaseType()) {
						case message:
							if (mgr != null) {
								mgr.onResumeSending();
							}
							response = ApptentiveClient.postMessage(contextRef.get(), (ApptentiveMessage) payload);
							if (mgr != null) {
								mgr.onSentMessage(contextRef.get(), (ApptentiveMessage) payload, response);
							}
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
								if (mgr != null) {
									mgr.onPauseSending(MessageManager.SEND_PAUSE_REASON_SERVER);
								}
								retryLater(NO_CONNECTION_SLEEP_TIME, contextRef.get());
								break;
							} else {
								retryLater(SERVER_ERROR_SLEEP_TIME, contextRef.get());
								break;
							}
						}
					}
				}
			} catch (Throwable throwable) {
				MetricModule.sendError(contextRef.get(), throwable, null, null);
			} finally
			{
				Log.v("Stopping PayloadSendThread.");
				threadRunning.set(false);
			}
		}

		private void retryLater(int millis, Context context) {
			Message msg = uiHandler.obtainMessage(UI_THREAD_MESSAGE_RETRY, context);
			uiHandler.removeMessages(UI_THREAD_MESSAGE_RETRY);
			uiHandler.sendMessageDelayed(msg, millis);
		}
	}

	public void appWentToForeground(Context context) {
		appInForeground.set(true);
		runPayloadSendRunnable(true, true, context);
	}

	public void appWentToBackground() {
		appInForeground.set(false);
		runPayloadSendRunnable(true, false, null);
	}

	public void setCanRunPayloadThread(Context context, boolean b) {
		threadCanRun.set(b);
		runPayloadSendRunnable(true, true, context);
	}
}
