/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.util.ApplicationInfo;
import com.apptentive.android.sdk.util.RuntimeUtils;
import com.apptentive.android.sdk.util.Util;

import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;

public class AppReleaseManager {

	public static AppRelease generateCurrentAppRelease(Context context, ApptentiveInternal apptentiveInternal) {

		AppRelease appRelease = new AppRelease();

		String appPackageName = context.getPackageName();
		int themeOverrideResId = context.getResources().getIdentifier("ApptentiveThemeOverride", "style", appPackageName);

		ApplicationInfo applicationInfo = RuntimeUtils.getApplicationInfo(context);

		appRelease.setAppStore(Util.getInstallerPackageName(context));
		appRelease.setDebug(applicationInfo.isDebuggable());
		appRelease.setIdentifier(appPackageName);
		if (apptentiveInternal != null) {
			appRelease.setInheritStyle(apptentiveInternal.isAppUsingAppCompatTheme());
		}
		appRelease.setOverrideStyle(themeOverrideResId != 0);
		appRelease.setTargetSdkVersion(String.valueOf(applicationInfo.getTargetSdkVersion()));
		appRelease.setType("android");
		appRelease.setVersionCode(applicationInfo.getVersionCode());
		appRelease.setVersionName(applicationInfo.getVersionName());

		return appRelease;
	}

	public static AppReleasePayload getPayload(AppRelease appRelease) {
		AppReleasePayload ret = new AppReleasePayload();
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

	// TODO: this method might not belong here
	public static com.apptentive.android.sdk.model.SdkAndAppReleasePayload getPayload(Sdk sdk, AppRelease appRelease) {
		com.apptentive.android.sdk.model.SdkAndAppReleasePayload ret = new com.apptentive.android.sdk.model.SdkAndAppReleasePayload();
		if (appRelease == null) {
			return ret;
		}

		// sdk data
		ret.setAuthorEmail(sdk.getAuthorEmail());
		ret.setAuthorName(sdk.getAuthorName());
		ret.setDistribution(sdk.getDistribution());
		ret.setDistributionVersion(sdk.getDistributionVersion());
		ret.setPlatform(sdk.getPlatform());
		ret.setProgrammingLanguage(sdk.getProgrammingLanguage());
		ret.setVersion(sdk.getVersion());


		// app release data
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
