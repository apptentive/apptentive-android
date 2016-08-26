/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.push;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class Parse extends ApptentiveTestCaseBase {

	private static final String pushDataApptentive = "{\"alert\":\"The body.\",\"apptentive\":\"{\\\"action\\\": \\\"pmc\\\"}\",\"parsePushId\":\"3CMOjEfT0q\",\"push_hash\":\"8b61309380e7cffc5291b272497e3c15\",\"title\":\"The title.\"}";
	private static final String pushDataNonApptentive = "{\"alert\":\"The body.\",\"parsePushId\":\"3CMOjEfT0q\",\"push_hash\":\"8b61309380e7cffc5291b272497e3c15\",\"title\":\"The title.\"}";
	private static final String pushDataCorrupt = "{";

	@Test
	public void apptentiveIntent() {
		Intent intent = new Intent();
		intent.putExtras(generatePushBundleApptentive());
		PendingIntent pendingIntent = Apptentive.buildPendingIntentFromPushNotification(intent);
		assertNotNull(pendingIntent);
		assertTrue(Apptentive.isApptentivePushNotification(intent));
		assertEquals(Apptentive.getTitleFromApptentivePush(intent), "The title.");
		assertEquals(Apptentive.getBodyFromApptentivePush(intent), "The body.");
	}

	@Test
	public void nonApptentiveIntent() {
		Intent intent = new Intent();
		intent.putExtras(generatePushBundleNonApptentive());
		PendingIntent pendingIntent = Apptentive.buildPendingIntentFromPushNotification(intent);
		assertNull(pendingIntent);
		assertFalse(Apptentive.isApptentivePushNotification(intent));
		assertEquals(Apptentive.getTitleFromApptentivePush(intent), "The title.");
		assertEquals(Apptentive.getBodyFromApptentivePush(intent), "The body.");
	}

	@Test
	public void apptentiveBundle() {
		Bundle bundle = generatePushBundleApptentive();
		assertNotNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertTrue(Apptentive.isApptentivePushNotification(bundle));
		assertEquals(Apptentive.getTitleFromApptentivePush(bundle), "The title.");
		assertEquals(Apptentive.getBodyFromApptentivePush(bundle), "The body.");
	}

	@Test
	public void nonApptentiveBundle() {
		Bundle bundle = generatePushBundleNonApptentive();
		assertNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertFalse(Apptentive.isApptentivePushNotification(bundle));
		assertEquals(Apptentive.getTitleFromApptentivePush(bundle), "The title.");
		assertEquals(Apptentive.getBodyFromApptentivePush(bundle), "The body.");
	}

	@Test
	public void nullIntent() {
		Intent intent = null;
		assertNull(Apptentive.buildPendingIntentFromPushNotification(intent));
		assertFalse(Apptentive.isApptentivePushNotification(intent));
		assertNull(Apptentive.getTitleFromApptentivePush(intent));
		assertNull(Apptentive.getBodyFromApptentivePush(intent));
	}

	@Test
	public void nullBundle() {
		Bundle bundle = null;
		assertNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertFalse(Apptentive.isApptentivePushNotification(bundle));
		assertNull(Apptentive.getTitleFromApptentivePush(bundle));
		assertNull(Apptentive.getBodyFromApptentivePush(bundle));
	}

	@Test
	public void corruptBundle() {
		Bundle bundle = new Bundle();
		bundle.putString("com.parse.Data", pushDataCorrupt);
		assertNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertFalse(Apptentive.isApptentivePushNotification(bundle));
		assertNull(Apptentive.getTitleFromApptentivePush(bundle));
		assertNull(Apptentive.getBodyFromApptentivePush(bundle));
	}

	private Bundle generatePushBundleApptentive() {
		Bundle bundle = new Bundle();
		bundle.putString("com.parse.Data", pushDataApptentive);
		bundle.putString("com.parse.Channel", "apptentive");
		return bundle;
	}

	private Bundle generatePushBundleNonApptentive() {
		Bundle bundle = new Bundle();
		bundle.putString("com.parse.Data", pushDataNonApptentive);
		return bundle;
	}
}
