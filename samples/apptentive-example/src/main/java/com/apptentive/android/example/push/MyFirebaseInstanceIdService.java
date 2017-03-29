/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.example.push;

import android.util.Log;

import com.apptentive.android.example.ExampleApplication;
import com.apptentive.android.sdk.Apptentive;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
	@Override
	public void onTokenRefresh() {
		String token = FirebaseInstanceId.getInstance().getToken();
		Log.e(ExampleApplication.TAG, "Refreshed InstanceId token: " + token);
		Apptentive.setPushNotificationIntegration(Apptentive.PUSH_PROVIDER_APPTENTIVE, token);
	}
}
