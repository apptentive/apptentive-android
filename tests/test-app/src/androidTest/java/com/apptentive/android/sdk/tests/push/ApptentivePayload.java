/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.push;

import android.os.Bundle;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;

public class ApptentivePayload extends ApptentiveInstrumentationTestCase {

	private static final String good = "{\"action\":\"pmc\"}";
	private static final String unknownAction = "{\"action\":\"foo\"}";
	private static final String wrongKey = "{\"foo\":\"pmc\"}";
	private static final String corrupt = "{\"foo:pmc";

	public void testMissingApptentive() {
		Bundle bundle = new Bundle();
		assertNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertFalse(Apptentive.isApptentivePushNotification(bundle));
	}

	public void testGood() {
		Bundle bundle = new Bundle();
		bundle.putString("apptentive", good);
		assertNotNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertTrue(Apptentive.isApptentivePushNotification(bundle));
	}

	// These three are tricky, because they are not valid actions, but it's still an Apptentive push.
	public void testUnknownAction() {
		Bundle bundle = new Bundle();
		bundle.putString("apptentive", unknownAction);
		assertNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertTrue(Apptentive.isApptentivePushNotification(bundle));
	}

	public void testWrongKey() {
		Bundle bundle = new Bundle();
		bundle.putString("apptentive", wrongKey);
		assertNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertTrue(Apptentive.isApptentivePushNotification(bundle));
	}

	public void testCorrupt() {
		Bundle bundle = new Bundle();
		bundle.putString("apptentive", corrupt);
		assertNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertTrue(Apptentive.isApptentivePushNotification(bundle));
	}
}
