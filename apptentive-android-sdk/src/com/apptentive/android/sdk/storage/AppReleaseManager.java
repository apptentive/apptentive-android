/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.AppRelease;
import com.apptentive.android.sdk.util.Constants;

/**
 * @author Sky Kelsey
 */
public class AppReleaseManager {

	public static AppRelease storeAppReleaseAndReturnDiff(Context context) {
		AppRelease original = getStoredAppRelease(context);
		AppRelease current = generateCurrentAppRelease(context);
		AppRelease diff = diffAppRelease(original, current);
		if(diff != null) {
			storeAppRelease(context, current);
			return diff;
		}
		return null;
	}

	private static AppRelease generateCurrentAppRelease(Context context) {
		AppRelease appRelease = new AppRelease();

		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			appRelease.setVersion(""+packageInfo.versionName);
			appRelease.setBuildNumber(""+packageInfo.versionCode);
			appRelease.setTargetSdkVersion(""+packageInfo.applicationInfo.targetSdkVersion);
		} catch (PackageManager.NameNotFoundException e) {
			Log.e("Can't load PackageInfo.", e);
		}
		return appRelease;
	}

	private static AppRelease getStoredAppRelease(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String appReleaseString = prefs.getString(Constants.PREF_KEY_APP_RELEASE, null);
		try {
			return new AppRelease(appReleaseString);
		} catch (Exception e) {
		}
		return null;
	}

	private static void storeAppRelease(Context context, AppRelease appRelease) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_APP_RELEASE, appRelease.toString()).commit();
	}

	private static AppRelease diffAppRelease(AppRelease older, AppRelease newer) {
		if(older == null) {
			return newer;
		}

		AppRelease ret = new AppRelease();
		int baseEntries = ret.length();

		String version = chooseLatest(older.getVersion(), newer.getVersion());
		if (version != null) {
			ret.setVersion(version);
		}

		String buildNumber = chooseLatest(older.getBuildNumber(), newer.getBuildNumber());
		if (buildNumber != null) {
			ret.setBuildNumber(buildNumber);
		}

		String targetSdkVersion = chooseLatest(older.getTargetSdkVersion(), newer.getTargetSdkVersion());
		if (targetSdkVersion != null) {
			ret.setTargetSdkVersion(targetSdkVersion);
		}

		// If there were no differences, return null.
		if(ret.length() <= baseEntries) {
			return null;
		}
		return ret;
	}

	/**
	 * A convenience method.
	 *
	 * @return newer - if it is different from old. <p/>empty string - if there was an old value, but not a newer value. This clears the old value.<p/> null - if there is no difference.
	 */
	private static String chooseLatest(String old, String newer) {
		if (old == null || old.equals("")) {
			old = null;
		}
		if (newer == null || newer.equals("")) {
			newer = null;
		}

		// New value.
		if (old != null && newer != null && !old.equals(newer)) {
			return newer;
		}

		// Clear existing value.
		if (old != null && newer == null) {
			return "";
		}

		if (old == null && newer != null) {
			return newer;
		}

		// Do nothing.
		return null;
	}
}
