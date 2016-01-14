/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.example.push;

import android.app.IntentService;
import android.content.Intent;

import com.apptentive.android.example.R;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

/**
 * @author Sky Kelsey
 */
public class RegistrationIntentService extends IntentService {

	private static final String TAG = "RegistrationIntentService";

	public RegistrationIntentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		InstanceID instanceID = InstanceID.getInstance(this);
		try {
			String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
			Apptentive.setPushNotificationIntegration(getApplicationContext(), Apptentive.PUSH_PROVIDER_APPTENTIVE, token);
		} catch (IOException e) {
			Log.e("Unable to get instanceId token.", e);
		}
	}
}
