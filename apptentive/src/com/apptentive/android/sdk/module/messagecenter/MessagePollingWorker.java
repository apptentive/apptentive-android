/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;

import android.content.Context;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Util;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sky Kelsey
 */
public class MessagePollingWorker {

  private static MessagePollingThread sPollingThread;

  // The following booleans will be accessed by both ui thread and worker thread
	private static AtomicBoolean appInForeground = new AtomicBoolean(false);
	private static AtomicBoolean messageCenterInForeground = new AtomicBoolean(false);
	private static AtomicBoolean threadRunning = new AtomicBoolean(false);

	// A synchronized getter/setter to the static instance of thread object
	public static synchronized MessagePollingThread getAndSetMessagePollingThread(boolean expect,
																																					boolean create,
																																					Context context) {
		if (expect && create && context != null) {
			sPollingThread = createPollingThread(context.getApplicationContext());
		} else if (!expect) {
			sPollingThread = null;
		}
		return sPollingThread;
	}

	private static MessagePollingThread createPollingThread(final Context context) {
		MessagePollingThread newThread = new MessagePollingThread(context);
		Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
				MetricModule.sendError(context, throwable, null, null);
			}
		};
		newThread.setUncaughtExceptionHandler(handler);
		newThread.setName("Apptentive-MessagePollingWorker");
		newThread.start();
		return newThread;
	}


	private static class MessagePollingThread extends Thread {
		private WeakReference<Context> contextRef;
		private long backgroundPollingInterval = -1;
		private long foregroundPollingInterval = -1;
		private Configuration conf;

		public MessagePollingThread(Context context) {
			contextRef = new WeakReference<>(context);
			conf = Configuration.load(context);
			backgroundPollingInterval = conf.getMessageCenterBgPoll() * 1000;
			foregroundPollingInterval = conf.getMessageCenterFgPoll() * 1000;
		}

		public void run() {
			try {
					Log.v("Started %s", toString());

					while (appInForeground.get()) {
						if (contextRef.get() == null) {
							threadRunning.set(false);
							return;
						}
						MessagePollingThread thread = getAndSetMessagePollingThread(true, false, null);
						if (thread != null && thread != MessagePollingThread.this) {
							return;
						}
						long pollingInterval = messageCenterInForeground.get() ? foregroundPollingInterval : backgroundPollingInterval;
						if (Util.isNetworkConnectionPresent(contextRef.get()) && Apptentive.canShowMessageCenter(contextRef.get())) {
							Log.v("Checking server for new messages every %d seconds", pollingInterval / 1000);
							MessageManager.fetchAndStoreMessages(contextRef.get(), messageCenterInForeground.get(), conf.isMessageCenterNotificationPopupEnabled());
						}
						MessagePollingWorker.goToSleep(pollingInterval);
					}
			} finally {
				threadRunning.set(false);
				Log.v("Stopping MessagePollingThread.");
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
		Log.v("Waking MessagePollingThread.");
		MessagePollingThread thread = getAndSetMessagePollingThread(true, false, null);
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}
	}

	// Called from main UI thread to create a new worker thread
	public static void appWentToForeground(Context context) {
		appInForeground.set(true);
		if (threadRunning.compareAndSet(false, true)) {
			/* appInForeground was "false", and set to "true"
			*  thread was not running, and set to be running
			*/
			getAndSetMessagePollingThread(true, true, context);
		} else {
			wakeUp();
		}
	}


	public static void appWentToBackground() {
		appInForeground.set(false);
		wakeUp();
	}

	/**
	 * If coming from the background, wake the thread so that it immediately starts runs and runs more often. If coming
	 * from the foreground, let the polling interval timeout naturally, at which point the polling interval will become
	 * the background polling interval.
	 *
	 * @param bInForeground true if the worker should be in foreground polling mode, else false.
	 */
	public static void setMessageCenterInForeground(boolean bInForeground) {
		if (!messageCenterInForeground.getAndSet(bInForeground) && bInForeground) {
			/* bInForeground is "true" && messageCenterInForeground was false
			*  Thread will wake up, then continue the while loop and proceed with fetching
			*/
			wakeUp();
		}
	}
}
