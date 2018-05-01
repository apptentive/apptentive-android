/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.notifications;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.ObjectUtils;
import com.apptentive.android.sdk.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.apptentive.android.sdk.ApptentiveLogTag.*;

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

	ApptentiveNotificationCenter() {
		this.observerListLookup = new HashMap<>();
	}

	//region Observers

	/**
	 * Adds an entry to the receiver’s dispatch table with an observer using strong reference.
	 */
	public synchronized ApptentiveNotificationCenter addObserver(String notification, ApptentiveNotificationObserver observer) {
		addObserver(notification, observer, false);
		return this;
	}

	/**
	 * Adds an entry to the receiver’s dispatch table with an observer.
	 *
	 * @param useWeakReference - weak reference is used if <code>true</code>
	 */
	public synchronized void addObserver(String notification, ApptentiveNotificationObserver observer, boolean useWeakReference) {
		final ApptentiveNotificationObserverList list = resolveObserverList(notification);
		list.addObserver(observer, useWeakReference);
	}

	/**
	 * Removes matching entries from the receiver’s dispatch table.
	 */
	public synchronized void removeObserver(final String notification, final ApptentiveNotificationObserver observer) {
		final ApptentiveNotificationObserverList list = findObserverList(notification);
		if (list != null) {
			list.removeObserver(observer);
		}
	}

	/**
	 * Removes all the entries specifying a given observer from the receiver’s dispatch table.
	 */
	public synchronized void removeObserver(final ApptentiveNotificationObserver observer) {
		for (ApptentiveNotificationObserverList observers : observerListLookup.values()) {
			observers.removeObserver(observer);
		}
	}

	//endregion

	//region Notifications

	/**
	 * Creates a notification with a given name and posts it to the receiver.
	 */
	public synchronized void postNotification(String name) {
		postNotification(name, EMPTY_USER_INFO);
	}

	/**
	 * Creates a notification with a given name and user info and posts it to the receiver.
	 */
	public synchronized void postNotification(final String name, Object... args) {
		postNotification(name, ObjectUtils.toMap(args));
	}

	/**
	 * Creates a notification with a given name and user info and posts it to the receiver.
	 */
	public synchronized void postNotification(final String name, final Map<String, Object> userInfo) {
		ApptentiveLog.v(NOTIFICATIONS, "Post notification: name=%s userInfo={%s}", name, StringUtils.toString(userInfo));

		final ApptentiveNotificationObserverList list = findObserverList(name);
		if (list != null) {
			list.notifyObservers(new ApptentiveNotification(name, userInfo));
		}
	}

	//endregion

	//region Helpers

	/**
	 * Find an observer list for the specified name.
	 *
	 * @return <code>null</code> is not found
	 */
	private synchronized ApptentiveNotificationObserverList findObserverList(String name) {
		return observerListLookup.get(name);
	}

	/**
	 * Find an observer list for the specified name or creates a new one if not found.
	 */
	private synchronized ApptentiveNotificationObserverList resolveObserverList(String name) {
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
		static final ApptentiveNotificationCenter INSTANCE = new ApptentiveNotificationCenter();
	}

	//endregion
}
