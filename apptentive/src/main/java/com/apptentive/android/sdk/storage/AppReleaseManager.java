/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.Util;

public class AppReleaseManager {

	public static AppRelease generateCurrentAppRelease(Context context) {

		AppRelease appRelease = new AppRelease();

		String appPackageName = context.getPackageName();
		PackageManager packageManager = context.getPackageManager();

		int currentVersionCode = 0;
		String currentVersionName = "0";
		int targetSdkVersion = 0;
		boolean isAppDebuggable = false;
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(appPackageName, PackageManager.GET_META_DATA | PackageManager.GET_RECEIVERS);
			ApplicationInfo ai = packageInfo.applicationInfo;
			currentVersionCode = packageInfo.versionCode;
			currentVersionName = packageInfo.versionName;
			targetSdkVersion = packageInfo.applicationInfo.targetSdkVersion;
			Bundle metaData = ai.metaData;
			if (metaData != null) {
				isAppDebuggable = (ai.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
			}
		} catch (PackageManager.NameNotFoundException e) {
			ApptentiveLog.e("Failed to read app's PackageInfo.");
		}

		int themeOverrideResId = context.getResources().getIdentifier("ApptentiveThemeOverride", "style", appPackageName);

		appRelease.setAppStore(Util.getInstallerPackageName(context));
		appRelease.setDebug(isAppDebuggable);
		appRelease.setIdentifier(appPackageName);
		appRelease.setInheritStyle(ApptentiveInternal.getInstance().isAppUsingAppCompatTheme());
		appRelease.setOverrideStyle(themeOverrideResId != 0);
		appRelease.setTargetSdkVersion(String.valueOf(targetSdkVersion));
		appRelease.setType("android");
		appRelease.setVersionCode(currentVersionCode);
		appRelease.setVersionName(currentVersionName);

		return appRelease;
	}

	public static com.apptentive.android.sdk.model.AppRelease getPayload(AppRelease appRelease) {
		com.apptentive.android.sdk.model.AppRelease ret = new com.apptentive.android.sdk.model.AppRelease();
		if (appRelease == null) {
			return ret;
		}

		ret.setAppStore(appRelease.getAppStore());
		ret.setDebug(appRelease.isDebug());
		ret.setIdentifier(appRelease.getIdentifier());
		ret.setInheritStyle(appRelease.isInheritStyle());
		ret.setOverrideStyle(appRelease.isOverrideStyle());
		ret.setTargetSdkVersion(appRelease.getTargetSdkVersion());
		ret.setType(appRelease.getType());
		ret.setVersionCode(appRelease.getVersionCode());
		ret.setVersionName(appRelease.getVersionName());
		return ret;
	}
}
