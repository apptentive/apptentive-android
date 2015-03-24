/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.push;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class TestPushNotifications extends ApptentiveInstrumentationTestCase {

	public void testUrbanAirshipPushNotificationIdentification() {
		Log.e("Running test: testUrbanAirshipPushNotificationIdentification()\n\n");

		// Null bundle push
		{
			resetDevice();
			Bundle bundle = null;
			assertFalse(Apptentive.isApptentivePushNotification(bundle));
			assertFalse(Apptentive.setPendingPushNotification(getTargetContext(), bundle));
		}

		// Non-Apptentive push.
		{
			resetDevice();
			Bundle bundle = new Bundle();
			bundle.putString("foo", "bar");
			assertFalse(Apptentive.isApptentivePushNotification(bundle));
			assertFalse(Apptentive.setPendingPushNotification(getTargetContext(), bundle));
		}

		// Invalid Apptentive push. Verify this is benign in UI test with access to Activity.
		{
			resetDevice();
			Bundle bundle = new Bundle();
			bundle.putString("apptentive", "foo");
			assertTrue(Apptentive.isApptentivePushNotification(bundle));
			assertTrue(Apptentive.setPendingPushNotification(getTargetContext(), bundle));
		}

		// Valid Apptentive push. Verify this can display in a UI test.
		{
			resetDevice();
			Bundle bundle = new Bundle();
			bundle.putString("apptentive", "{\"action\": \"pmc\"}");
			Apptentive.isApptentivePushNotification(bundle);
			assertTrue(Apptentive.isApptentivePushNotification(bundle));
			assertTrue(Apptentive.setPendingPushNotification(getTargetContext(), bundle));
		}
	}

	public void testParsePushNotificationIdentification() {
		Log.e("Running test: testParsePushNotificationIdentification()\n\n");

		// Null Intent
		{
			resetDevice();
			Intent intent = null;
			assertFalse(Apptentive.isApptentivePushNotification(intent));
			assertFalse(Apptentive.setPendingPushNotification(getTargetContext(), intent));
		}

		// Not a push
		{
			resetDevice();
			Intent intent = new Intent();
			assertFalse(Apptentive.isApptentivePushNotification(intent));
			assertFalse(Apptentive.setPendingPushNotification(getTargetContext(), intent));
		}

		// Non-Apptentive push
		try {
			resetDevice();
			JSONObject parseExtraJson = new JSONObject();
			parseExtraJson.put("foo", "bar");
			Intent intent = new Intent();
			intent.putExtra("com.parse.Data", parseExtraJson.toString());
			assertFalse(Apptentive.isApptentivePushNotification(intent));
			assertFalse(Apptentive.setPendingPushNotification(getTargetContext(), intent));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

		// Invalid Apptentive push. Verify this is benign in UI test with access to Activity.
		try {
			resetDevice();
			JSONObject parseExtraJson = new JSONObject();
			parseExtraJson.put("apptentive", "foo");
			Intent intent = new Intent();
			intent.putExtra("com.parse.Data", parseExtraJson.toString());
			assertTrue(Apptentive.isApptentivePushNotification(intent));
			assertTrue(Apptentive.setPendingPushNotification(getTargetContext(), intent));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

		// Valid Apptentive push
		try {
			resetDevice();
			JSONObject parseExtraJson = new JSONObject();
			parseExtraJson.put("apptentive", "{\"action\": \"pmc\"}");
			Intent intent = new Intent();
			intent.putExtra("com.parse.Data", parseExtraJson.toString());
			assertTrue(Apptentive.isApptentivePushNotification(intent));
			assertTrue(Apptentive.setPendingPushNotification(getTargetContext(), intent));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public void testAwsSnsPushNotificationIdentification() {
		Log.e("Running test: testAwsSnsPushNotificationIdentification()\n\n");

		// Null Intent
		{
			resetDevice();
			Intent intent = null;
			assertFalse(Apptentive.isApptentivePushNotification(intent));
			assertFalse(Apptentive.setPendingPushNotification(getTargetContext(), intent));
		}

		// Not a push
		{
			resetDevice();
			Intent intent = new Intent();
			assertFalse(Apptentive.isApptentivePushNotification(intent));
			assertFalse(Apptentive.setPendingPushNotification(getTargetContext(), intent));
		}

		// Invalid Apptentive push. Verify this is benign in UI test with access to Activity.
		{
			resetDevice();
			Intent intent = new Intent();
			intent.putExtra("apptentive", "foo");
			assertTrue(Apptentive.isApptentivePushNotification(intent));
			assertTrue(Apptentive.setPendingPushNotification(getTargetContext(), intent));
		}

		// Valid Apptentive push
		{
			resetDevice();
			Intent intent = new Intent();
			intent.putExtra("apptentive", "{\"action\": \"pmc\"}");
			assertTrue(Apptentive.isApptentivePushNotification(intent));
			assertTrue(Apptentive.setPendingPushNotification(getTargetContext(), intent));
		}
	}
}
