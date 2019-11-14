/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.app.NotificationManager;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.debug.Assert;

import static android.content.Context.NOTIFICATION_SERVICE;

public class ContextUtils {
	public static NotificationManager getNotificationManager(@NonNull Context context) {
		return getSystemService(context, NOTIFICATION_SERVICE, NotificationManager.class);
	}

	private static @Nullable <T> T getSystemService(@NonNull Context context, @NonNull String name, @NonNull Class<? extends T> cls) {
		Object service = context.getSystemService(name);
		Assert.assertTrue(cls.isInstance(service), "Unexpected service class: %s", cls);
		return ObjectUtils.as(service, cls);
	}
}
