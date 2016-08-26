/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.push;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;

import java.util.HashMap;
import java.util.Map;

public class Fcm extends ApptentiveInstrumentationTestCase {

	public void testPushDataApptentive() {
		Map<String, String> data = new HashMap<>();
		data.put("apptentive", "{\"action\":\"pmc\"}");
		data.put("title", "The title.");
		data.put("body", "The body.");
		assertNotNull(Apptentive.buildPendingIntentFromPushNotification(data));
		assertTrue(Apptentive.isApptentivePushNotification(data));
		assertEquals(Apptentive.getTitleFromApptentivePush(data), "The title.");
		assertEquals(Apptentive.getBodyFromApptentivePush(data), "The body.");
	}

	public void testPushDataNonApptentive() {
		Map<String, String> data = new HashMap<>();
		assertNull(Apptentive.buildPendingIntentFromPushNotification(data));
		assertFalse(Apptentive.isApptentivePushNotification(data));
		assertNull(Apptentive.getTitleFromApptentivePush(data));
		assertNull(Apptentive.getBodyFromApptentivePush(data));
	}

	public void testNullPushData() {
		Map<String, String> data = null;
		assertNull(Apptentive.buildPendingIntentFromPushNotification(data));
		assertFalse(Apptentive.isApptentivePushNotification(data));
		assertNull(Apptentive.getTitleFromApptentivePush(data));
		assertNull(Apptentive.getBodyFromApptentivePush(data));
	}
}
