/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.notifications;

import com.apptentive.android.sdk.TestCaseBase;

import org.junit.Test;

import java.util.HashMap;

public class ApptentiveNotificationObserverListTest extends TestCaseBase {

	private static final boolean WEAK_REFERENCE = true;
	private static final boolean STRONG_REFERENCE = false;

	@Test
	public void testAddNotifyObservers() {
		ApptentiveNotificationObserverList list = new ApptentiveNotificationObserverList();

		Observer o1 = new Observer("observer1");
		Observer o2 = new Observer("observer2");

		list.addObserver(o1, WEAK_REFERENCE);
		list.addObserver(o2, STRONG_REFERENCE);

		// trying to add duplicates
		list.addObserver(o1, WEAK_REFERENCE);
		list.addObserver(o1, STRONG_REFERENCE);

		list.addObserver(o2, WEAK_REFERENCE);
		list.addObserver(o2, STRONG_REFERENCE);

		list.notifyObservers(new ApptentiveNotification("notification", new HashMap<String, Object>()));
		assertResult("observer1", "observer2");
	}

	@Test
	public void testAddNotifyObserversWithLostReferences() {
		ApptentiveNotificationObserverList list = new ApptentiveNotificationObserverList();

		// begin of the scope
		{
			Observer o1 = new Observer("observer1");
			Observer o4 = new Observer("observer4");

			list.addObserver(o1, WEAK_REFERENCE); // this reference won't be lost until the end of the current scope
			list.addObserver(new Observer("observer2"), WEAK_REFERENCE); // this reference would be lost right away
			list.addObserver(new Observer("observer3"), STRONG_REFERENCE); // this reference won't be lost
			list.addObserver(o4, STRONG_REFERENCE);

			// force GC so the weak reference becomes null
			System.gc();

			list.notifyObservers(new ApptentiveNotification("notification", new HashMap<String, Object>()));
			assertResult("observer1", "observer3", "observer4");

			o1 = o4 = null; // this step is necessary for a proper GC
		}
		// end of the scope

		// force GC so the weak reference becomes null
		System.gc();

		list.notifyObservers(new ApptentiveNotification("notification", new HashMap<String, Object>()));
		assertResult("observer3", "observer4");
	}


	private class Observer implements ApptentiveNotificationObserver {

		private final String name;

		public Observer(String name) {
			this.name = name;
		}

		@Override
		public void onReceiveNotification(ApptentiveNotification notification) {
			addResult(name);
		}
	}
}