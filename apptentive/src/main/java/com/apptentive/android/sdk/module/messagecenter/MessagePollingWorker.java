/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;


import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.module.metric.MetricModule;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sky Kelsey
 */
public class MessagePollingWorker {

	private MessagePollingThread sPollingThread;

	// The following booleans will be accessed by both ui thread and worker thread
	private AtomicBoolean appInForeground = new AtomicBoolean(false);
	public AtomicBoolean messageCenterInForeground = new AtomicBoolean(false);
	private AtomicBoolean threadRunning = new AtomicBoolean(false);

	private MessageManager manager;

	public MessagePollingWorker(MessageManager manager) {
		this.manager = manager;
	}

	// A synchronized getter/setter to the static instance of thread object
	public synchronized MessagePollingThread getAndSetMessagePollingThread(boolean expect,
																		   boolean create) {
		if (expect && create) {
			sPollingThread = createPollingThread();
		} else if (!expect) {
			sPollingThread = null;
		}
		return sPollingThread;
	}

	private MessagePollingThread createPollingThread() {
		MessagePollingThread newThread = new MessagePollingThread();
		Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
				MetricModule.sendError(throwable, null, null);
			}
		};
		newThread.setUncaughtExceptionHandler(handler);
		newThread.setName("Apptentive-MessagePollingWorker");
		newThread.start();
		return newThread;
	}


	private class MessagePollingThread extends Thread {

		private long backgroundPollingInterval = -1;
		private long foregroundPollingInterval = -1;
		private Configuration conf;

		public MessagePollingThread() {
			conf = Configuration.load();
			backgroundPollingInterval = conf.getMessageCenterBgPoll() * 1000;
			foregroundPollingInterval = conf.getMessageCenterFgPoll() * 1000;
		}

		public void run() {
			try {
				ApptentiveLog.v("Started %s", toString());

				while (appInForeground.get()) {
					MessagePollingThread thread = getAndSetMessagePollingThread(true, false);
					if (thread != null && thread != MessagePollingThread.this) {
						return;
					}
					long pollingInterval = messageCenterInForeground.get() ? foregroundPollingInterval : backgroundPollingInterval;
					if (Apptentive.canShowMessageCenter()) {
						ApptentiveLog.v("Checking server for new messages every %d seconds", pollingInterval / 1000);
						manager.fetchAndStoreMessages(messageCenterInForeground.get(), conf.isMessageCenterNotificationPopupEnabled());
					}
					goToSleep(pollingInterval);
				}
			} finally {
				threadRunning.set(false);
				sPollingThread = null;
				ApptentiveLog.v("Stopping MessagePollingThread.");
			}
		}
	}

	private void goToSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// This is normal and happens whenever we wake the thread with an interrupt.
		}
	}

	private void wakeUp() {
		ApptentiveLog.v("Waking MessagePollingThread.");
		MessagePollingThread thread = getAndSetMessagePollingThread(true, false);
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}
	}

	// Called from main UI thread to create a new worker thread
	public void appWentToForeground() {
		appInForeground.set(true);
		if (threadRunning.compareAndSet(false, true)) {
			/* appInForeground was "false", and set to "true"
			*  thread was not running, and set to be running
			*/
			getAndSetMessagePollingThread(true, true);
		} else {
			wakeUp();
		}
	}


	public void appWentToBackground() {
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
	public void setMessageCenterInForeground(boolean bInForeground) {
		if (!messageCenterInForeground.getAndSet(bInForeground) && bInForeground) {
			/* bInForeground is "true" && messageCenterInForeground was false
			*  Thread will wake up, then continue the while loop and proceed with fetching
			*/
			wakeUp();
		}
	}
}
