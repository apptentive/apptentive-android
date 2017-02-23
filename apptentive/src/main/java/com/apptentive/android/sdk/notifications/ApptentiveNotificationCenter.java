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

	private static final Map<String, Object> EMPTY_USER_INFO = Collections.emptyMap();

	private final Map<String, ApptentiveNotificationObserverList> observerListLookup;
	private final DispatchQueue operationQueue;

	ApptentiveNotificationCenter(DispatchQueue operationQueue) {
		if (operationQueue == null) {
			throw new IllegalArgumentException("Operation queue is not defined");
		}

		this.observerListLookup = new HashMap<>();
		this.operationQueue = operationQueue;
	}

	//region Observers

	public void addObserver(final String notification, final ApptentiveNotificationObserver observer) {
		operationQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				final ApptentiveNotificationObserverList list = resolveObserverList(notification);
				list.addObserver(observer);
			}
		});
	}

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

	public void postNotification(String name) {
		postNotification(name, EMPTY_USER_INFO);
	}

	public void postNotification(String name, Map<String, Object> userInfo) {
		postNotification(new ApptentiveNotification(name, userInfo));
	}

	public void postNotification(final ApptentiveNotification notification) {
		operationQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				final ApptentiveNotificationObserverList list = findObserverList(notification.getName());
				if (list != null) {
					list.notifyObservers(notification);
				}
			}
		});
	}

	//endregion

	//region Helpers

	private ApptentiveNotificationObserverList findObserverList(String name) {
		return observerListLookup.get(name);
	}

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

	public static ApptentiveNotificationCenter defaultCenter() {
		return Holder.INSTANCE;
	}

	/**
	 * Thread-safe initialization trick
	 */
	private static class Holder {
		static final ApptentiveNotificationCenter INSTANCE = new ApptentiveNotificationCenter(DispatchQueue.mainQueue());
	}

	//endregion
}
