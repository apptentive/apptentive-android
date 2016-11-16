/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.SharedPreferences;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.model.AppRelease;
import com.apptentive.android.sdk.util.Constants;

public class AppReleaseManager {

	public static void storeAppRelease(AppRelease appRelease) {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		prefs.edit().putString(Constants.PREF_KEY_APP_RELEASE, appRelease.toString()).apply();
	}
}
