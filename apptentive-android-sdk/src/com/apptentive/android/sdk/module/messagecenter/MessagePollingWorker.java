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
import com.apptentive.android.sdk.util.Constants;

/**
 * @author Sky Kelsey
 */
public class MessagePollingWorker {

	private static Context appContext;
	private static boolean running;

	private static boolean foreground = false;

	private static long backgroundPollingInterval = Constants.CONFIG_DEFAULT_MESSAGE_CENTER_BG_POLL_SECONDS * 1000;
	private static long foregroundPollingInterval = Constants.CONFIG_DEFAULT_MESSAGE_CENTER_FG_POLL_SECONDS * 1000;


	public static synchronized void doStart(Context context) {
		appContext = context.getApplicationContext();

		if (!running) {
			Log.i("Starting MessagePollingWorker.");

			Configuration conf = Configuration.load(context);
			backgroundPollingInterval = conf.getMessageCenterBgPoll() * 1000;
			foregroundPollingInterval = conf.getMessageCenterFgPoll() * 1000;

			running = true;
			Thread messagePollingThreadRunner = new MessagePollingThread();
			Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread thread, Throwable throwable) {
					MetricModule.sendError(appContext, throwable, null, null);
				}
			};
			messagePollingThreadRunner.setUncaughtExceptionHandler(handler);
			messagePollingThreadRunner.setName("Apptentive-MessagePollingWorker");
			messagePollingThreadRunner.start();
		}
	}

	private static class MessagePollingThread extends Thread {
		public void run() {
			try {
				synchronized (this) {
					if(appContext == null) {
						return;
					}
					while (runningActivities > 0) {
						long pollingInterval = foreground ? foregroundPollingInterval : backgroundPollingInterval;
						MessageManager.fetchAndStoreMessages(appContext);
						pause(pollingInterval);
					}
				}
			} finally {
				running = false;
			}
		}
	}

	private static void pause(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

	private static long runningActivities = 0;

	public static void start(Context context) {
		runningActivities++;
		Log.e("Start message polling. Running Activities = %d", runningActivities);
		doStart(context);
	}

	public static void stop() {
		runningActivities--;
		Log.e("Stop message polling. Running Activities = %d", runningActivities);
	}

	public static void setForeground(boolean foreground) {
		MessagePollingWorker.foreground = foreground;
	}
}
