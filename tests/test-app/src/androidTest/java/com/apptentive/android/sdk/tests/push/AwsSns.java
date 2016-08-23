/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.push;

import android.os.Bundle;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;

public class AwsSns extends ApptentiveInstrumentationTestCase {

	public void testPushIntentApptentive() {
		Bundle bundle = generateBasePushBundle();
		bundle.putString("apptentive", "{\"action\":\"pmc\"}");
		bundle.putString("title", "The title.");
		bundle.putString("body", "The body.");
		assertNotNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertTrue(Apptentive.isApptentivePushNotification(bundle));
		assertEquals(Apptentive.getTitleFromApptentivePush(bundle), "The title.");
		assertEquals(Apptentive.getBodyFromApptentivePush(bundle), "The body.");
	}

	public void testPushIntentNonApptentive() {
		Bundle bundle = generateBasePushBundle();
		assertNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertFalse(Apptentive.isApptentivePushNotification(bundle));
		assertNull(Apptentive.getTitleFromApptentivePush(bundle));
		assertNull(Apptentive.getBodyFromApptentivePush(bundle));
	}

	public void testNullBundle() {
		Bundle bundle = null;
		assertNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertFalse(Apptentive.isApptentivePushNotification(bundle));
		assertNull(Apptentive.getTitleFromApptentivePush(bundle));
		assertNull(Apptentive.getBodyFromApptentivePush(bundle));
	}

	private Bundle generateBasePushBundle() {
		Bundle bundle = new Bundle();
		bundle.putString("google.sent_time", "1471319839889");
		bundle.putString("google.message_id", "0:1471319839895646%9c295d74f9fd7ecd");
		bundle.putString("collapse_key", "do_not_collapse");
		return bundle;
	}
}
