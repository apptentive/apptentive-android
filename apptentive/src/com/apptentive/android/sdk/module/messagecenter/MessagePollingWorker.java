/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;

import android.content.Context;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey
 */
public class MessagePollingWorker {

	private static Context appContext;
	private static MessagePollingThread messagePollingThread;
	private static boolean appInForeground;
	private static boolean threadRunning;
	private static boolean messageCenterInForeground;
	private static long backgroundPollingInterval = -1;
	private static long foregroundPollingInterval = -1;

	public static synchronized void doStart(Context context) {
		appContext = context.getApplicationContext();
		if (!threadRunning) {
			Log.i("Starting MessagePollingWorker.");

			if (backgroundPollingInterval == -1 || foregroundPollingInterval == -1) {
				Configuration conf = Configuration.load(context);
				backgroundPollingInterval = conf.getMessageCenterBgPoll() * 1000;
				foregroundPollingInterval = conf.getMessageCenterFgPoll() * 1000;
			}

			threadRunning = true;
			messagePollingThread = new MessagePollingThread();
			Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread thread, Throwable throwable) {
					MetricModule.sendError(appContext, throwable, null, null);
				}
			};
			messagePollingThread.setUncaughtExceptionHandler(handler);
			messagePollingThread.setName("Apptentive-MessagePollingWorker");
			messagePollingThread.start();
		}
	}

	private static class MessagePollingThread extends Thread {
		public void run() {
			try {
				synchronized (this) {
					Log.v("Started %s", toString());
					if (appContext == null) {
						return;
					}
					while (appInForeground) {
						long pollingInterval = messageCenterInForeground ? foregroundPollingInterval : backgroundPollingInterval;
						Configuration conf = Configuration.load(appContext);
						if (Util.isNetworkConnectionPresent(appContext) && conf.isMessageCenterEnabled(appContext)) {
							Log.v("Checking server for new messages every %d seconds", pollingInterval / 1000);
							MessageManager.fetchAndStoreMessages(appContext);
						}
						MessagePollingWorker.goToSleep(pollingInterval);
					}
				}
			} finally {
				Log.v("Stopping MessagePollingThread.");
				threadRunning = false;
			}
		}
	}

	private static void goToSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// This is normal and happens whenever we wake the thread with an interrupt.
		}
	}

	private static void wakeUp() {
		if (messagePollingThread != null) {
			Log.v("Waking MessagePollingThread.");
			messagePollingThread.interrupt();
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

	/**
	 * If coming from the background, wake the thread so that it immediately starts runs and runs more often. If coming
	 * from the foreground, let the polling interval timeout naturally, at which point the polling interval will become
	 * the background polling interval.
	 *
	 * @param messageCenterInForeground true if the worker should be in foreground polling mode, else false.
	 */
	public static void setMessageCenterInForeground(boolean messageCenterInForeground) {
		boolean enteringForeground = messageCenterInForeground && !MessagePollingWorker.messageCenterInForeground;
		MessagePollingWorker.messageCenterInForeground = messageCenterInForeground;
		if (enteringForeground) {
			wakeUp();
		}
	}
}
