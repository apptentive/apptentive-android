/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.notifications;

import com.apptentive.android.sdk.LogicTestCaseBase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import static org.junit.Assert.*;

public class ApptentiveNotificationObserverListTest extends LogicTestCaseBase {

	private static final boolean WEAK_REFERENCE = true;
	private static final boolean STRONG_REFERENCE = false;

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void testAddObservers() {
		ApptentiveNotificationObserverList list = new ApptentiveNotificationObserverList();

		Observer o1 = new Observer("observer1");
		Observer o2 = new Observer("observer2");

		list.addObserver(o1, WEAK_REFERENCE);
		list.addObserver(o2, STRONG_REFERENCE);
		assertEquals(2, list.size());

		// trying to add duplicates
		list.addObserver(o1, WEAK_REFERENCE);
		list.addObserver(o1, STRONG_REFERENCE);
		assertEquals(2, list.size());

		list.addObserver(o2, WEAK_REFERENCE);
		list.addObserver(o2, STRONG_REFERENCE);
		assertEquals(2, list.size());

		list.notifyObservers(new ApptentiveNotification("notification", new HashMap<String, Object>()));
		assertResult("observer1", "observer2");
	}

	@Test
	public void testRemoveObservers() {
		ApptentiveNotificationObserverList list = new ApptentiveNotificationObserverList();

		Observer o1 = new Observer("observer1");
		Observer o2 = new Observer("observer2");

		list.addObserver(o1, WEAK_REFERENCE);
		list.addObserver(o2, STRONG_REFERENCE);

		list.notifyObservers(new ApptentiveNotification("notification", new HashMap<String, Object>()));
		assertResult("observer1", "observer2");
		assertEquals(2, list.size());

		list.removeObserver(o1);
		assertEquals(1, list.size());

		list.notifyObservers(new ApptentiveNotification("notification", new HashMap<String, Object>()));
		assertResult("observer2");

		list.removeObserver(o2);
		list.notifyObservers(new ApptentiveNotification("notification", new HashMap<String, Object>()));
		assertResult();
		assertEquals(0, list.size());
	}

	@Test
	public void testWeakReferences() {
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
			assertEquals(3, list.size());

			o1 = o4 = null; // this step is necessary for a proper GC
		}
		// end of the scope

		// force GC so the weak reference becomes null
		System.gc();

		list.notifyObservers(new ApptentiveNotification("notification", new HashMap<String, Object>()));
		assertResult("observer3", "observer4");
		assertEquals(2, list.size());
	}

	@Test
	public void testConcurrentModification() {
		final ApptentiveNotificationObserverList list = new ApptentiveNotificationObserverList();

		final Observer o1 = new Observer("observer1");
		final Observer o2 = new Observer("observer2");

		list.addObserver(new ApptentiveNotificationObserver() {
			@Override
			public void onReceiveNotification(ApptentiveNotification notification) {
				list.removeObserver(o1);
				addResult("anonymous-observer1");
			}
		}, STRONG_REFERENCE);
		list.addObserver(o1, WEAK_REFERENCE);
		list.addObserver(new ApptentiveNotificationObserver() {
			@Override
			public void onReceiveNotification(ApptentiveNotification notification) {
				list.removeObserver(o2);
				addResult("anonymous-observer2");
			}
		}, STRONG_REFERENCE);
		list.addObserver(o2, STRONG_REFERENCE);

		list.notifyObservers(new ApptentiveNotification("notification", new HashMap<String, Object>()));
		assertResult("anonymous-observer1", "observer1", "anonymous-observer2", "observer2");
		assertEquals(2, list.size());

		list.notifyObservers(new ApptentiveNotification("notification", new HashMap<String, Object>()));
		assertResult("anonymous-observer1", "anonymous-observer2");
		assertEquals(2, list.size());
	}

	@Test
	public void testThrowingException() {
		final ApptentiveNotificationObserverList list = new ApptentiveNotificationObserverList();

		final Observer o1 = new Observer("observer1");
		final Observer o2 = new Observer("observer2");

		list.addObserver(o1, STRONG_REFERENCE);
		list.addObserver(new ApptentiveNotificationObserver() {
			@Override
			public void onReceiveNotification(ApptentiveNotification notification) {
				addResult("error");
				throw new RuntimeException("Error");
			}
		}, STRONG_REFERENCE);
		list.addObserver(o2, STRONG_REFERENCE);

		list.notifyObservers(new ApptentiveNotification("notification", new HashMap<String, Object>()));
		assertResult("observer1", "error", "observer2");
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