/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.push;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import androidx.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class UrbanAirship extends ApptentiveTestCaseBase {

	@Before
	public void setUp() {
		super.setUp();
		ApptentiveInternal.setInstance(new ApptentiveInternal(targetContext));
	}

/* TODO: Decouple tested code from Conversation and MessageManager
	@Test
	public void apptentiveIntent() {
		Intent intent = generatePushIntentApptentive();
		assertNotNull(Apptentive.buildPendingIntentFromPushNotification(intent));
		assertTrue(Apptentive.isApptentivePushNotification(intent));
		assertEquals("The title.", Apptentive.getTitleFromApptentivePush(intent));
		assertEquals("The body.", Apptentive.getBodyFromApptentivePush(intent));
	}
*/

	@Test
	public void nonApptentiveIntent() {
		Intent intent = generatePushIntentNonApptentive();
		Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
			@Override
			public void onPendingIntent(PendingIntent pendingIntent) {
				assertNull(pendingIntent);
			}
		}, intent);
		assertFalse(Apptentive.isApptentivePushNotification(intent));
		assertEquals("The title.", Apptentive.getTitleFromApptentivePush(intent));
		assertEquals("The body.", Apptentive.getBodyFromApptentivePush(intent));
	}

/* TODO: Decouple tested code from Conversation and MessageManager
	@Test
	public void apptentiveBundle() {
		Bundle bundle = generatePushBundleApptentive();
		assertNotNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertTrue(Apptentive.isApptentivePushNotification(bundle));
		assertEquals("The title.", Apptentive.getTitleFromApptentivePush(bundle));
		assertEquals("The body.", Apptentive.getBodyFromApptentivePush(bundle));
	}
*/

	@Test
	public void nonApptentiveBundle() {
		Bundle bundle = generatePushBundleNonApptentive();
		Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
			@Override
			public void onPendingIntent(PendingIntent pendingIntent) {
				assertNull(pendingIntent);
			}
		}, bundle);
		assertFalse(Apptentive.isApptentivePushNotification(bundle));
		assertEquals("The title.", Apptentive.getTitleFromApptentivePush(bundle));
		assertEquals("The body.", Apptentive.getBodyFromApptentivePush(bundle));
	}

/* TODO: Decouple tested code from Conversation and MessageManager
	@Test
	public void apptentiveExtraMessageBundle() {
		Bundle bundle = generatePushExtraMessageBundleApptentive();
		assertNotNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		assertTrue(Apptentive.isApptentivePushNotification(bundle));
		assertEquals("The title.", Apptentive.getTitleFromApptentivePush(bundle));
		assertEquals("The body.", Apptentive.getBodyFromApptentivePush(bundle));
	}
*/

	@Test
	public void nonApptentiveExtraMessageBundle() {
		Bundle bundle = generatePushExtraMessageBundleNonApptentive();
		Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
			@Override
			public void onPendingIntent(PendingIntent pendingIntent) {
				assertNull(pendingIntent);
			}
		}, bundle);
		assertFalse(Apptentive.isApptentivePushNotification(bundle));
		assertEquals("The title.", Apptentive.getTitleFromApptentivePush(bundle));
		assertEquals("The body.", Apptentive.getBodyFromApptentivePush(bundle));
	}

	@Test
	public void nullIntent() {
		Intent intent = null;
		Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
			@Override
			public void onPendingIntent(PendingIntent pendingIntent) {
				assertNull(pendingIntent);
			}
		}, intent);
		assertFalse(Apptentive.isApptentivePushNotification(intent));
		assertNull(Apptentive.getTitleFromApptentivePush(intent));
		assertNull(Apptentive.getBodyFromApptentivePush(intent));
	}

	@Test
	public void nullBundle() {
		Bundle bundle = null;
		Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
			@Override
			public void onPendingIntent(PendingIntent pendingIntent) {
				assertNull(pendingIntent);
			}
		}, bundle);
		assertFalse(Apptentive.isApptentivePushNotification(bundle));
		assertNull(Apptentive.getTitleFromApptentivePush(bundle));
		assertNull(Apptentive.getBodyFromApptentivePush(bundle));
	}

	@Test
	public void missingBundle() {
		Intent intent = new Intent();
		Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
			@Override
			public void onPendingIntent(PendingIntent pendingIntent) {
				assertNull(pendingIntent);
			}
		}, intent);
		assertFalse(Apptentive.isApptentivePushNotification(intent));
		assertNull(Apptentive.getTitleFromApptentivePush(intent));
		assertNull(Apptentive.getBodyFromApptentivePush(intent));
	}

	@Test
	public void missingExtraMessageBundle() {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		intent.putExtras(bundle);
		Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
			@Override
			public void onPendingIntent(PendingIntent pendingIntent) {
				assertNull(pendingIntent);
			}
		}, intent);
		assertFalse(Apptentive.isApptentivePushNotification(intent));
		assertNull(Apptentive.getTitleFromApptentivePush(intent));
		assertNull(Apptentive.getBodyFromApptentivePush(intent));
	}

	private Intent generatePushIntentApptentive() {
		Intent ret = new Intent();
		ret.setAction("com.urbanairship.push.RECEIVED");
		ret.putExtras(generatePushBundleApptentive());
		return ret;
	}

	private Intent generatePushIntentNonApptentive() {
		Intent ret = new Intent();
		ret.setAction("com.urbanairship.push.RECEIVED");
		ret.putExtras(generatePushBundleNonApptentive());
		return ret;
	}

	private Bundle generatePushBundleApptentive() {
		Bundle ret = new Bundle();
		ret.putInt("com.urbanairship.push.NOTIFICATION_ID", 1000);
		ret.putBundle("com.urbanairship.push.EXTRA_PUSH_MESSAGE_BUNDLE", generatePushExtraMessageBundleApptentive());
		return ret;
	}

	private Bundle generatePushBundleNonApptentive() {
		Bundle ret = new Bundle();
		ret.putInt("com.urbanairship.push.NOTIFICATION_ID", 1000);
		ret.putBundle("com.urbanairship.push.EXTRA_PUSH_MESSAGE_BUNDLE", generatePushExtraMessageBundleNonApptentive());
		return ret;
	}

	private Bundle generatePushExtraMessageBundleApptentive() {
		Bundle ret = generatePushExtraBundleBase();
		ret.putString("title", "The title.");
		ret.putString("apptentive", "{\"action\": \"pmc\"}");
		return ret;
	}

	private Bundle generatePushExtraMessageBundleNonApptentive() {
		return generatePushExtraBundleBase();
	}

	private Bundle generatePushExtraBundleBase() {
		Bundle ret = new Bundle();
		ret.putString("com.urbanairship.push.APID", "68553d5e-96df-4b1d-bd6c-a1d0a87fd10c");
		ret.putString("com.urbanairship.push.PUSH_ID", "05ba6970-618b-11e6-8c8d-14feb5d26692");
		ret.putString("com.urbanairship.push.CANONICAL_PUSH_ID", "2ce9d5c6-c7b3-4dd7-92a7-f7123faa04fd");
		ret.putString("com.urbanairship.push.ALERT", "The body.");
		ret.putString("title", "The title.");
		ret.putString("collapse_key", "do_not_collapse");
		ret.putString("google.message_id", "0:1471116040604919%735dfe72f9fd7ecd");
		ret.putLong("google.sent_time", 1471116040597L);
		ret.putLong("from", 335329484622L);
		return ret;
	}
}
