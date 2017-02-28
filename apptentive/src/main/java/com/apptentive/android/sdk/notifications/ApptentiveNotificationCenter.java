/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.notifications;

import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An {@link ApptentiveNotificationCenter} object (or simply, notification center) provides a
 * mechanism for broadcasting information within a program. An {@link ApptentiveNotificationCenter}
 * object is essentially a notification dispatch table.
 */
public class ApptentiveNotificationCenter {

	/**
	 * Shared empty user data for notification.
	 */
	private static final Map<String, Object> EMPTY_USER_INFO = Collections.emptyMap();

	/**
	 * Lookup table for notification-to-observers search.
	 */
	private final Map<String, ApptentiveNotificationObserverList> observerListLookup;

	/**
	 * Dispatch queue for posting notifications.
	 */
	private final DispatchQueue notificationQueue;

	/**
	 * Dispatch queue for the concurrent access to the internal data structures
	 * (adding/removing observers, etc).
	 */
	private final DispatchQueue operationQueue;

	ApptentiveNotificationCenter(DispatchQueue notificationQueue, DispatchQueue operationQueue) {
		if (notificationQueue == null) {
			throw new IllegalArgumentException("Notification queue is not defined");
		}

		if (operationQueue == null) {
			throw new IllegalArgumentException("Operation queue is not defined");
		}

		this.observerListLookup = new HashMap<>();
		this.notificationQueue = notificationQueue;
		this.operationQueue = operationQueue;
	}

	//region Observers

	/**
	 * Adds an entry to the receiver’s dispatch table with an observer using strong reference.
	 */
	public void addObserver(final String notification, final ApptentiveNotificationObserver observer) {
		addObserver(notification, observer, false);
	}

	/**
	 * Adds an entry to the receiver’s dispatch table with an observer.
	 *
	 * @param useWeakReference - weak reference is used if <code>true</code>
	 */
	public void addObserver(final String notification, final ApptentiveNotificationObserver observer, final boolean useWeakReference) {
		operationQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				final ApptentiveNotificationObserverList list = resolveObserverList(notification);
				list.addObserver(observer, useWeakReference);
			}
		});
	}

	/**
	 * Removes matching entries from the receiver’s dispatch table.
	 */
	public void removeObserver(final String notification, final ApptentiveNotificationObserver observer) {
		operationQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				final ApptentiveNotificationObserverList list = findObserverList(notification);
				if (list != null) {
					list.removeObserver(observer);
				}
			}
		});
	}

	/**
	 * Removes all the entries specifying a given observer from the receiver’s dispatch table.
	 */
	public void removeObserver(final ApptentiveNotificationObserver observer) {
		operationQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				for (ApptentiveNotificationObserverList observers : observerListLookup.values()) {
					observers.removeObserver(observer);
				}
			}
		});
	}

	//endregion

	//region Notifications

	/**
	 * Posts a given notification to the receiver.
	 */
	public void postNotification(String name) {
		postNotification(name, EMPTY_USER_INFO);
	}

	/**
	 * Creates a notification with a given name and information and posts it to the receiver.
	 */
	public void postNotification(String name, Map<String, Object> userInfo) {
		postNotification(new ApptentiveNotification(name, userInfo));
	}

	/**
	 * Posts a given notification to the receiver.
	 */
	public void postNotification(final ApptentiveNotification notification) {
		operationQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				if (notificationQueue == operationQueue) { // is it the same queue?
					postNotificationSync(notification);
				} else {
					notificationQueue.dispatchAsync(new DispatchTask() {
						@Override
						protected void execute() {
							postNotificationSync(notification);
						}
					});
				}
			}
		});
	}

	// this method is not thread-safe
	private void postNotificationSync(ApptentiveNotification notification) {
		final ApptentiveNotificationObserverList list = findObserverList(notification.getName());
		if (list != null) {
			list.notifyObservers(notification);
		}
	}

	//endregion

	//region Helpers

	/**
	 * Find an observer list for the specified name.
	 *
	 * @return <code>null</code> is not found
	 */
	private ApptentiveNotificationObserverList findObserverList(String name) {
		return observerListLookup.get(name);
	}

	/**
	 * Find an observer list for the specified name or creates a new one if not found.
	 */
	private ApptentiveNotificationObserverList resolveObserverList(String name) {
		ApptentiveNotificationObserverList list = observerListLookup.get(name);
		if (list == null) {
			list = new ApptentiveNotificationObserverList();
			observerListLookup.put(name, list);
		}
		return list;
	}

	//endregion

	//region Singleton

	/**
	 * Returns the process’s default notification center.
	 */
	public static ApptentiveNotificationCenter defaultCenter() {
		return Holder.INSTANCE;
	}

	/**
	 * Thread-safe initialization trick
	 */
	private static class Holder {
		static final ApptentiveNotificationCenter INSTANCE = new ApptentiveNotificationCenter(DispatchQueue.mainQueue(), DispatchQueue.mainQueue());
	}

	//endregion
}
