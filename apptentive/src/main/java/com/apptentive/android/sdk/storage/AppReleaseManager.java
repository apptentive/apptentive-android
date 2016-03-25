/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.AppRelease;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.JsonDiffer;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class AppReleaseManager {

	public static AppRelease storeAppReleaseAndReturnDiff() {
		AppRelease stored = getStoredAppRelease();
		AppRelease current = generateCurrentAppRelease();

		Object diff = JsonDiffer.getDiff(stored, current);
		if(diff != null) {
			try {
				storeAppRelease(current);
				return new AppRelease(diff.toString());
			} catch (JSONException e) {
				ApptentiveLog.e("Error casting to AppRelease.", e);
			}
		}
		return null;
	}

	private static AppRelease generateCurrentAppRelease() {
		AppRelease appRelease = new AppRelease();

		try {
			Context context = ApptentiveInternal.getInstance().getApplicationContext();
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			appRelease.setVersion(packageInfo.versionName);
			appRelease.setIdentifier(packageInfo.packageName);
			appRelease.setBuildNumber(String.valueOf(packageInfo.versionCode));
			appRelease.setTargetSdkVersion(String.valueOf(packageInfo.applicationInfo.targetSdkVersion));
			appRelease.setAppStore(Util.getInstallerPackageName(context));
		} catch (PackageManager.NameNotFoundException e) {
			ApptentiveLog.e("Can't load PackageInfo.", e);
		}
		return appRelease;
	}

	public static AppRelease getStoredAppRelease() {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		String appReleaseString = prefs.getString(Constants.PREF_KEY_APP_RELEASE, null);
		try {
			return new AppRelease(appReleaseString);
		} catch (Exception e) {
			// Ignore
		}
		return null;
	}

	private static void storeAppRelease(AppRelease appRelease) {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		prefs.edit().putString(Constants.PREF_KEY_APP_RELEASE, appRelease.toString()).apply();
	}
}
