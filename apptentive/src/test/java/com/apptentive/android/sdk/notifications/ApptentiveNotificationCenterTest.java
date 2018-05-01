/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.notifications;

import com.apptentive.android.sdk.LogicTestCaseBase;
import com.apptentive.android.sdk.util.ObjectUtils;
import com.apptentive.android.sdk.util.StringUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ApptentiveNotificationCenterTest extends LogicTestCaseBase {

	private ApptentiveNotificationCenter notificationCenter;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		notificationCenter = new ApptentiveNotificationCenter();
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	private static final boolean WEAK_REFERENCE = true;
	private static final boolean STRONG_REFERENCE = false;

	@Test
	public void testPostNotifications() {
		Observer o1 = new Observer("observer1");
		Observer o2 = new Observer("observer2");
		Observer o3 = new Observer("observer3");
		Observer o4 = new Observer("observer4");

		notificationCenter.addObserver("notification1", o1, WEAK_REFERENCE);
		notificationCenter.addObserver("notification1", o2, STRONG_REFERENCE);

		notificationCenter.addObserver("notification2", o3, WEAK_REFERENCE);
		notificationCenter.addObserver("notification2", o4, STRONG_REFERENCE);

		notificationCenter.postNotification("notification1", ObjectUtils.toMap("key1", "value1"));
		assertResult("observer1: notification1 {'key1':'value1'}", "observer2: notification1 {'key1':'value1'}");

		notificationCenter.postNotification("notification2", ObjectUtils.toMap("key2", "value2"));
		assertResult("observer3: notification2 {'key2':'value2'}", "observer4: notification2 {'key2':'value2'}");

		notificationCenter.postNotification("notification3");
		assertResult();

		// remove some observers
		notificationCenter.removeObserver(o1);
		notificationCenter.removeObserver(o4);

		notificationCenter.postNotification("notification1", ObjectUtils.toMap("key1", "value1"));
		assertResult("observer2: notification1 {'key1':'value1'}");

		notificationCenter.postNotification("notification2", ObjectUtils.toMap("key2", "value2"));
		assertResult("observer3: notification2 {'key2':'value2'}");

		notificationCenter.postNotification("notification3");
		assertResult();

		// remove the rest
		notificationCenter.removeObserver("notification2", o2); // hit & miss
		notificationCenter.removeObserver("notification1", o3); // hit & miss

		notificationCenter.removeObserver("notification1", o2);
		notificationCenter.removeObserver("notification2", o3);

		notificationCenter.postNotification("notification1", ObjectUtils.toMap("key1", "value1"));
		assertResult();

		notificationCenter.postNotification("notification2", ObjectUtils.toMap("key2", "value2"));
		assertResult();

		notificationCenter.postNotification("notification3");
		assertResult();

		// add back
		notificationCenter.addObserver("notification1", o1, WEAK_REFERENCE);
		notificationCenter.addObserver("notification1", o2, STRONG_REFERENCE);

		notificationCenter.addObserver("notification2", o3, WEAK_REFERENCE);
		notificationCenter.addObserver("notification2", o4, STRONG_REFERENCE);

		notificationCenter.postNotification("notification1", ObjectUtils.toMap("key1", "value1"));
		assertResult("observer1: notification1 {'key1':'value1'}", "observer2: notification1 {'key1':'value1'}");

		notificationCenter.postNotification("notification2", ObjectUtils.toMap("key2", "value2"));
		assertResult("observer3: notification2 {'key2':'value2'}", "observer4: notification2 {'key2':'value2'}");

		notificationCenter.postNotification("notification3");
		assertResult();
	}

	private class Observer implements ApptentiveNotificationObserver {

		private final String name;

		public Observer(String name) {
			this.name = name;
		}

		@Override
		public void onReceiveNotification(ApptentiveNotification notification) {
			addResult(String.format("%s: %s {%s}", name, notification.getName(), StringUtils.toString(notification.getUserInfo())));
		}
	}
}