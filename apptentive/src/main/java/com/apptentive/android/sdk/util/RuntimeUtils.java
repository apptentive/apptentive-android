/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.apptentive.android.sdk.ApptentiveLog;

/**
 * Collection of helper functions for Android runtime queries.
 */
public class RuntimeUtils {
	/**
	 * Returns <code>true</code> is the app is running in a debug mode
	 */
	public static boolean isAppDebuggable(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		try {
			final String appPackageName = context.getPackageName();
			final PackageManager packageManager = context.getPackageManager();

			PackageInfo packageInfo = packageManager.getPackageInfo(appPackageName, PackageManager.GET_META_DATA | PackageManager.GET_RECEIVERS);
			ApplicationInfo ai = packageInfo.applicationInfo;
			Bundle metaData = ai.metaData;
			if (metaData != null) {
				return (ai.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
			}
		} catch (Exception e) {
			ApptentiveLog.e("Failed to read app's PackageInfo.");
		}

		return false;
	}
}
