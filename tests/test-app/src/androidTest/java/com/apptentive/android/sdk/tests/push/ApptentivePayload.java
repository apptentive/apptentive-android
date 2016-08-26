/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.push;

import android.os.Bundle;
import android.support.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ApptentivePayload extends ApptentiveTestCaseBase {

	private static final String good = "{\"action\":\"pmc\"}";
	private static final String unknownAction = "{\"action\":\"foo\"}";
	private static final String wrongKey = "{\"foo\":\"pmc\"}";
	private static final String corrupt = "{\"foo:pmc";

	@Test
	public void missingApptentive() {
		Bundle bundle = new Bundle();
		assertNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertFalse(Apptentive.isApptentivePushNotification(bundle));
	}

	@Test
	public void good() {
		Bundle bundle = new Bundle();
		bundle.putString("apptentive", good);
		assertNotNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertTrue(Apptentive.isApptentivePushNotification(bundle));
	}

	// These three are tricky, because they are not valid actions, but it's still an Apptentive push.
	@Test
	public void unknownAction() {
		Bundle bundle = new Bundle();
		bundle.putString("apptentive", unknownAction);
		assertNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertTrue(Apptentive.isApptentivePushNotification(bundle));
	}

	@Test
	public void wrongActionKey() {
		Bundle bundle = new Bundle();
		bundle.putString("apptentive", wrongKey);
		assertNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertTrue(Apptentive.isApptentivePushNotification(bundle));
	}

	@Test
	public void corrupt() {
		Bundle bundle = new Bundle();
		bundle.putString("apptentive", corrupt);
		assertNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertTrue(Apptentive.isApptentivePushNotification(bundle));
	}
}