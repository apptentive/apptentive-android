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
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class Gcm extends ApptentiveTestCaseBase {

/* TODO: Decouple tested code from Conversation and MessageManager
	@Test
	public void apptentivePush() {
		Bundle bundle = generateBasePushBundle();
		bundle.putString("apptentive", "{\"action\":\"pmc\"}");
		bundle.putString("title", "The title.");
		bundle.putString("body", "The body.");
		assertNotNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertTrue(Apptentive.isApptentivePushNotification(bundle));
		assertEquals(Apptentive.getTitleFromApptentivePush(bundle), "The title.");
		assertEquals(Apptentive.getBodyFromApptentivePush(bundle), "The body.");
	}
*/

	@Test
	public void nonApptentivePush() {
		Bundle bundle = generateBasePushBundle();
		assertNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertFalse(Apptentive.isApptentivePushNotification(bundle));
		assertNull(Apptentive.getTitleFromApptentivePush(bundle));
		assertNull(Apptentive.getBodyFromApptentivePush(bundle));
	}

	@Test
	public void nullBundle() {
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
