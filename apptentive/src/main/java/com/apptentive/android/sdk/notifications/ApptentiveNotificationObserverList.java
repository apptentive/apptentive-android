/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.notifications;

import com.apptentive.android.sdk.ApptentiveLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

class ApptentiveNotificationObserverList {
	private final List<WeakReference<ApptentiveNotificationObserver>> observerRefs;

	ApptentiveNotificationObserverList() {
		observerRefs = new ArrayList<>();
	}

	void notifyObservers(ApptentiveNotification notification) {
		boolean hasLostReferences = false;

		// create a temporary list of observers to avoid concurrent modification errors
		List<ApptentiveNotificationObserver> observers = new ArrayList<>(observerRefs.size());
		for (int i = 0; i < observerRefs.size(); ++i) {
			ApptentiveNotificationObserver observer = observerRefs.get(i).get();
			if (observer != null) {
				observers.add(observer);
			} else {
				hasLostReferences = true;
			}
		}

		// notify observers
		for (int i = 0; i < observers.size(); ++i) {
			try {
				observers.get(i).onReceiveNotification(notification);
			} catch (Exception e) {
				ApptentiveLog.e(e, "Exception while posting notification: %s", notification);
			}
		}

		// clean lost references
		if (hasLostReferences) {
			for (int i = observerRefs.size() - 1; i >= 0; --i) {
				if (observerRefs.get(i).get() == null) {
					observerRefs.remove(i);
				}
			}
		}
	}

	boolean addObserver(ApptentiveNotificationObserver observer) {
		if (observer == null) {
			throw new IllegalArgumentException("Observer is null");
		}

		if (!contains(observer)) {
			observerRefs.add(new WeakReference<>(observer));
			return true;
		}

		return false;
	}

	boolean removeObserver(ApptentiveNotificationObserver observer) {
		int index = indexOf(observer);
		if (index != -1) {
			observerRefs.remove(index);
			return true;
		}
		return false;
	}

	private int indexOf(ApptentiveNotificationObserver observer) {
		for (int i = 0; i < observerRefs.size(); ++i) {
			if (observerRefs.get(i).get() == observer) {
				return i;
			}
		}
		return -1;
	}

	private boolean contains(ApptentiveNotificationObserver observer) {
		return indexOf(observer) != -1;
	}
}
