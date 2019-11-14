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

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class OldStylePush extends ApptentiveTestCaseBase {

	@Before
	public void setUp() {
		super.setUp();
		ApptentiveInternal.setInstance(new ApptentiveInternal(targetContext));
	}

	@Test
	public void testUrbanAirshipPushNotificationIdentification() {
		// Null bundle push
		{
			Bundle bundle = null;
			assertFalse(Apptentive.isApptentivePushNotification(bundle));
			Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
				@Override
				public void onPendingIntent(PendingIntent pendingIntent) {
					assertNull(pendingIntent);
				}
			}, bundle);
		}

		// Non-Apptentive push.
		{
			Bundle bundle = new Bundle();
			bundle.putString("foo", "bar");
			assertFalse(Apptentive.isApptentivePushNotification(bundle));
			Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
				@Override
				public void onPendingIntent(PendingIntent pendingIntent) {
					assertNull(pendingIntent);
				}
			}, bundle);
		}

		// Invalid Apptentive push. Verify this is benign in UI test with access to Activity.
		{
			Bundle bundle = new Bundle();
			bundle.putString("apptentive", "foo");
			assertTrue(Apptentive.isApptentivePushNotification(bundle));
			Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
				@Override
				public void onPendingIntent(PendingIntent pendingIntent) {
					assertNull(pendingIntent);
				}
			}, bundle);
		}

/* TODO: Decouple tested code from Conversation and MessageManager
		// Valid Apptentive push. Verify this can display in a UI test.
		{
			Bundle bundle = new Bundle();
			bundle.putString("apptentive", "{\"action\": \"pmc\"}");
			Apptentive.isApptentivePushNotification(bundle);
			assertTrue(Apptentive.isApptentivePushNotification(bundle));
			assertNotNull(Apptentive.buildPendingIntentFromPushNotification(bundle));
		}
*/
	}

	@Test
	public void testParsePushNotificationIdentification() throws JSONException {
		// Null Intent
		{
			Intent intent = null;
			assertFalse(Apptentive.isApptentivePushNotification(intent));
			Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
				@Override
				public void onPendingIntent(PendingIntent pendingIntent) {
					assertNull(pendingIntent);
				}
			}, intent);
		}

		// Not a push
		{
			Intent intent = new Intent();
			assertFalse(Apptentive.isApptentivePushNotification(intent));
			Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
				@Override
				public void onPendingIntent(PendingIntent pendingIntent) {
					assertNull(pendingIntent);
				}
			}, intent);
		}

		// Non-Apptentive push
		{
			JSONObject parseExtraJson = new JSONObject();
			parseExtraJson.put("foo", "bar");
			Intent intent = new Intent();
			intent.putExtra("com.parse.Data", parseExtraJson.toString());
			assertFalse(Apptentive.isApptentivePushNotification(intent));
			Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
				@Override
				public void onPendingIntent(PendingIntent pendingIntent) {
					assertNull(pendingIntent);
				}
			}, intent);
		}

		// Invalid Apptentive push. Verify this is benign in UI test with access to Activity.
		{
			JSONObject parseExtraJson = new JSONObject();
			parseExtraJson.put("apptentive", "foo");
			Intent intent = new Intent();
			intent.putExtra("com.parse.Data", parseExtraJson.toString());
			assertTrue(Apptentive.isApptentivePushNotification(intent));
			Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
				@Override
				public void onPendingIntent(PendingIntent pendingIntent) {
					assertNull(pendingIntent);
				}
			}, intent);
		}

/* TODO: Decouple tested code from Conversation and MessageManager
		// Valid Apptentive push
		{
			JSONObject parseExtraJson = new JSONObject();
			parseExtraJson.put("apptentive", "{\"action\": \"pmc\"}");
			Intent intent = new Intent();
			intent.putExtra("com.parse.Data", parseExtraJson.toString());
			assertTrue(Apptentive.isApptentivePushNotification(intent));
			assertNotNull(Apptentive.buildPendingIntentFromPushNotification(intent));
		}
*/
	}

	@Test
	public void testAwsSnsPushNotificationIdentification() {
		// Null Intent
		{
			Intent intent = null;
			assertFalse(Apptentive.isApptentivePushNotification(intent));
			Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
				@Override
				public void onPendingIntent(PendingIntent pendingIntent) {
					assertNull(pendingIntent);
				}
			}, intent);
		}

		// Not a push
		{
			Intent intent = new Intent();
			assertFalse(Apptentive.isApptentivePushNotification(intent));
			Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
				@Override
				public void onPendingIntent(PendingIntent pendingIntent) {
					assertNull(pendingIntent);
				}
			}, intent);
		}

		// Invalid Apptentive push. Verify this is benign in UI test with access to Activity.
		{
			Intent intent = new Intent();
			intent.putExtra("apptentive", "foo");
			assertTrue(Apptentive.isApptentivePushNotification(intent));
			Apptentive.buildPendingIntentFromPushNotification(new Apptentive.PendingIntentCallback() {
				@Override
				public void onPendingIntent(PendingIntent pendingIntent) {
					assertNull(pendingIntent);
				}
			}, intent);
		}

/* TODO: Decouple tested code from Conversation and MessageManager
		// Valid Apptentive push
		{
			Intent intent = new Intent();
			intent.putExtra("apptentive", "{\"action\": \"pmc\"}");
			assertTrue(Apptentive.isApptentivePushNotification(intent));
			assertNotNull(Apptentive.buildPendingIntentFromPushNotification(intent));
		}
*/
	}
}
