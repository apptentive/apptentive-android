/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.push;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class OldStylePush extends ApptentiveTestCaseBase {

	@Test
	public void testUrbanAirshipPushNotificationIdentification() {
		// Null bundle push
		{
			Bundle bundle = null;
			assertFalse(Apptentive.isApptentivePushNotification(bundle));
			assertFalse(Apptentive.setPendingPushNotification(bundle));
		}

		// Non-Apptentive push.
		{
			Bundle bundle = new Bundle();
			bundle.putString("foo", "bar");
			assertFalse(Apptentive.isApptentivePushNotification(bundle));
			assertFalse(Apptentive.setPendingPushNotification(bundle));
		}

		// Invalid Apptentive push. Verify this is benign in UI test with access to Activity.
		{
			Bundle bundle = new Bundle();
			bundle.putString("apptentive", "foo");
			assertTrue(Apptentive.isApptentivePushNotification(bundle));
			assertTrue(Apptentive.setPendingPushNotification(bundle));
		}

		// Valid Apptentive push. Verify this can display in a UI test.
		{
			Bundle bundle = new Bundle();
			bundle.putString("apptentive", "{\"action\": \"pmc\"}");
			Apptentive.isApptentivePushNotification(bundle);
			assertTrue(Apptentive.isApptentivePushNotification(bundle));
			assertTrue(Apptentive.setPendingPushNotification(bundle));
		}
	}

	@Test
	public void testParsePushNotificationIdentification() {
		// Null Intent
		{
			Intent intent = null;
			assertFalse(Apptentive.isApptentivePushNotification(intent));
			assertFalse(Apptentive.setPendingPushNotification(intent));
		}

		// Not a push
		{
			Intent intent = new Intent();
			assertFalse(Apptentive.isApptentivePushNotification(intent));
			assertFalse(Apptentive.setPendingPushNotification(intent));
		}

		// Non-Apptentive push
		try {
			JSONObject parseExtraJson = new JSONObject();
			parseExtraJson.put("foo", "bar");
			Intent intent = new Intent();
			intent.putExtra("com.parse.Data", parseExtraJson.toString());
			assertFalse(Apptentive.isApptentivePushNotification(intent));
			assertFalse(Apptentive.setPendingPushNotification(intent));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

		// Invalid Apptentive push. Verify this is benign in UI test with access to Activity.
		try {
			JSONObject parseExtraJson = new JSONObject();
			parseExtraJson.put("apptentive", "foo");
			Intent intent = new Intent();
			intent.putExtra("com.parse.Data", parseExtraJson.toString());
			assertTrue(Apptentive.isApptentivePushNotification(intent));
			assertTrue(Apptentive.setPendingPushNotification(intent));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

		// Valid Apptentive push
		try {
			JSONObject parseExtraJson = new JSONObject();
			parseExtraJson.put("apptentive", "{\"action\": \"pmc\"}");
			Intent intent = new Intent();
			intent.putExtra("com.parse.Data", parseExtraJson.toString());
			assertTrue(Apptentive.isApptentivePushNotification(intent));
			assertTrue(Apptentive.setPendingPushNotification(intent));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testAwsSnsPushNotificationIdentification() {
		// Null Intent
		{
			Intent intent = null;
			assertFalse(Apptentive.isApptentivePushNotification(intent));
			assertFalse(Apptentive.setPendingPushNotification(intent));
		}

		// Not a push
		{
			Intent intent = new Intent();
			assertFalse(Apptentive.isApptentivePushNotification(intent));
			assertFalse(Apptentive.setPendingPushNotification(intent));
		}

		// Invalid Apptentive push. Verify this is benign in UI test with access to Activity.
		{
			Intent intent = new Intent();
			intent.putExtra("apptentive", "foo");
			assertTrue(Apptentive.isApptentivePushNotification(intent));
			assertTrue(Apptentive.setPendingPushNotification(intent));
		}

		// Valid Apptentive push
		{
			Intent intent = new Intent();
			intent.putExtra("apptentive", "{\"action\": \"pmc\"}");
			assertTrue(Apptentive.isApptentivePushNotification(intent));
			assertTrue(Apptentive.setPendingPushNotification(intent));
		}
	}
}
