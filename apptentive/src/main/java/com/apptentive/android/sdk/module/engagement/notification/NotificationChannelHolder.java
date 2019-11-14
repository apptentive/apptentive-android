/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import androidx.annotation.RequiresApi;

import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_CHANNEL_DEFAULT;

@RequiresApi(26)
public class NotificationChannelHolder {

	private static NotificationChannel instance;

	static {
		NotificationChannel newInstance = new NotificationChannel(NOTIFICATION_CHANNEL_DEFAULT, "Apptentive Notifications", NotificationManager.IMPORTANCE_DEFAULT);
		newInstance.setDescription("Channel description");
		newInstance.enableLights(true);
		newInstance.setLightColor(Color.RED);
		newInstance.enableVibration(true);
		instance = newInstance;
	}

	public static NotificationChannel getInstance() {
		return instance;
	}
}
