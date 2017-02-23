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

class ApptentiveNotificationObserverList {
	private final List<ApptentiveNotificationObserver> observers;

	ApptentiveNotificationObserverList() {
		observers = new ArrayList<>();
	}

	void notifyObservers(ApptentiveNotification notification) {
		boolean hasLostReferences = false;

		// create a temporary list of observers to avoid concurrent modification errors
		List<ApptentiveNotificationObserver> temp = new ArrayList<>(observers.size());
		for (int i = 0; i < observers.size(); ++i) {
			ApptentiveNotificationObserver observer = observers.get(i);
			ObserverRef observerRef = ObjectUtils.as(observer, ObserverRef.class);
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
			}
		}

		// clean lost references
		if (hasLostReferences) {
			for (int i = observers.size() - 1; i >= 0; --i) {
				final ObserverRef observerRef = ObjectUtils.as(observers.get(i), ObserverRef.class);
				if (observerRef != null && observerRef.isReferenceLost()) {
					observers.remove(i);
				}
			}
		}
	}

	boolean addObserver(ApptentiveNotificationObserver observer, boolean weakReference) {
		if (observer == null) {
			throw new IllegalArgumentException("Observer is null");
		}

		if (!contains(observer)) {
			observers.add(weakReference ? new ObserverRef(observer) : observer);
			return true;
		}

		return false;
	}

	boolean removeObserver(ApptentiveNotificationObserver observer) {
		int index = indexOf(observer);
		if (index != -1) {
			observers.remove(index);
			return true;
		}
		return false;
	}

	public int size() {
		return observers.size();
	}

	private int indexOf(ApptentiveNotificationObserver observer) {
		for (int i = 0; i < observers.size(); ++i) {
			final ApptentiveNotificationObserver other = observers.get(i);
			if (other == observer) {
				return i;
			}

			final ObserverRef otherReference = ObjectUtils.as(other, ObserverRef.class);
			if (otherReference != null && otherReference.get() == observer) {
				return i;
			}
		}
		return -1;
	}

	private boolean contains(ApptentiveNotificationObserver observer) {
		return indexOf(observer) != -1;
	}

	private static class ObserverRef extends WeakReference<ApptentiveNotificationObserver> implements ApptentiveNotificationObserver {

		ObserverRef(ApptentiveNotificationObserver referent) {
			super(referent);
		}

		@Override
		public void onReceiveNotification(ApptentiveNotification notification) {
			ApptentiveNotificationObserver observer = get();
			if (observer != null) {
				observer.onReceiveNotification(notification);
			}
		}

		public boolean isReferenceLost() {
			return get() == null;
		}
	}
}
