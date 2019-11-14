/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

public class NotificationUtils {
	public static boolean isNotificationChannelEnabled(Context context, @NonNull String channelId){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			if (!StringUtils.isNullOrEmpty(channelId)) {
				NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationChannel channel = manager.getNotificationChannel(channelId);
				return channel == null || channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
			}
			return false;
		}

		return NotificationManagerCompat.from(context).areNotificationsEnabled();
	}
}
