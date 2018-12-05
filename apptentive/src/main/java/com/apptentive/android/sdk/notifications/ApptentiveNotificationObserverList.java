/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.notifications;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.ObjectUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

/**
 * Utility class for storing weak/strong references to {@link ApptentiveNotificationObserverList}
 * and posting notification. Lost reference cleanup is done automatically.
 */
class ApptentiveNotificationObserverList {

	/**
	 * List of observers.
	 */
	private final List<ApptentiveNotificationObserver> observers;

	ApptentiveNotificationObserverList() {
		observers = new ArrayList<>();
	}

	/**
	 * Posts notification to all observers.
	 */
	void notifyObservers(ApptentiveNotification notification) {
		boolean hasLostReferences = false;

		// create a temporary list of observers to avoid concurrent modification errors
		List<ApptentiveNotificationObserver> temp = new ArrayList<>(observers.size());
		for (int i = 0; i < observers.size(); ++i) {
			ApptentiveNotificationObserver observer = observers.get(i);
			ObserverWeakReference observerRef = ObjectUtils.as(observer, ObserverWeakReference.class);
			if (observerRef == null || !observerRef.isReferenceLost()) {
				temp.add(observer);
			} else {
				hasLostReferences = true;
			}
		}

		// notify observers
		for (int i = 0; i < temp.size(); ++i) {
			try {
				temp.get(i).onReceiveNotification(notification);
			} catch (Exception e) {
				ApptentiveLog.e(e, "Exception while posting notification: %s", notification);
				logException(e); // TODO: add more context info
			}
		}

		// clean lost references
		if (hasLostReferences) {
			for (int i = observers.size() - 1; i >= 0; --i) {
				final ObserverWeakReference observerRef = ObjectUtils.as(observers.get(i), ObserverWeakReference.class);
				if (observerRef != null && observerRef.isReferenceLost()) {
					observers.remove(i);
				}
			}
		}
	}

	/**
	 * Adds an observer to the list without duplicates.
	 *
	 * @param useWeakReference - use weak reference if <code>true</code>
	 * @return <code>true</code> - if observer was added
	 */
	boolean addObserver(ApptentiveNotificationObserver observer, boolean useWeakReference) {
		if (observer == null) {
			throw new IllegalArgumentException("Observer is null");
		}

		if (!contains(observer)) {
			observers.add(useWeakReference ? new ObserverWeakReference(observer) : observer);
			return true;
		}

		return false;
	}

	/**
	 * Removes observer os its weak reference from the list
	 *
	 * @return <code>true</code> if observer was returned
	 */
	boolean removeObserver(ApptentiveNotificationObserver observer) {
		int index = indexOf(observer);
		if (index != -1) {
			observers.remove(index);
			return true;
		}
		return false;
	}

	/**
	 * Size of the list
	 */
	public int size() {
		return observers.size();
	}

	/**
	 * Returns an index of the observer or its weak reference.
	 *
	 * @return -1 if not found
	 */
	private int indexOf(ApptentiveNotificationObserver observer) {
		for (int i = 0; i < observers.size(); ++i) {
			final ApptentiveNotificationObserver other = observers.get(i);
			if (other == observer) {
				return i;
			}

			final ObserverWeakReference otherReference = ObjectUtils.as(other, ObserverWeakReference.class);
			if (otherReference != null && otherReference.get() == observer) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Checks if observer or its weak references are in the list.
	 */
	private boolean contains(ApptentiveNotificationObserver observer) {
		return indexOf(observer) != -1;
	}

	/**
	 * Helper class for stored {@link ApptentiveNotificationObserver} weak reference
	 */
	private static class ObserverWeakReference extends WeakReference<ApptentiveNotificationObserver> implements ApptentiveNotificationObserver {

		ObserverWeakReference(ApptentiveNotificationObserver referent) {
			super(referent);
		}

		@Override
		public void onReceiveNotification(ApptentiveNotification notification) {
			ApptentiveNotificationObserver observer = get();
			if (observer != null) {
				observer.onReceiveNotification(notification);
			}
		}

		/**
		 * Returns true if observer's memory was freed.
		 */
		boolean isReferenceLost() {
			return get() == null;
		}
	}
}
