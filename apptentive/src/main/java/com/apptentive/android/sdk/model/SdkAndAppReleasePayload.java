/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;

/**
 * A combined payload of {@link SdkPayload} and {@link AppReleasePayload} payloads.
 * <p>
 * This class effectively contains the source code from both {@link SdkPayload}
 * and {@link AppReleasePayload} payloads (which still kept for backward compatibility
 * purposes).
 */
public class SdkAndAppReleasePayload extends JsonPayload {

	private static final String KEY_TYPE = "type";
	private static final String KEY_VERSION_NAME = "version_name";
	private static final String KEY_VERSION_CODE = "version_code";
	private static final String KEY_IDENTIFIER = "identifier";
	private static final String KEY_TARGET_SDK_VERSION = "target_sdk_version";
	private static final String KEY_APP_STORE = "app_store";
	private static final String KEY_STYLE_INHERIT = "inheriting_styles";
	private static final String KEY_STYLE_OVERRIDE = "overriding_styles";
	private static final String KEY_DEBUG = "debug";

	private static final String KEY_VERSION = "sdk_version";
	private static final String KEY_PROGRAMMING_LANGUAGE = "sdk_programming_language";
	private static final String KEY_AUTHOR_NAME = "sdk_author_name";
	private static final String KEY_AUTHOR_EMAIL = "sdk_author_email";
	private static final String KEY_PLATFORM = "sdk_platform";
	private static final String KEY_DISTRIBUTION = "sdk_distribution";
	private static final String KEY_DISTRIBUTION_VERSION = "sdk_distribution_version";

	public SdkAndAppReleasePayload() {
		super(PayloadType.sdk_and_app_release);
	}

	//region Http-request

	@Override
	public String getHttpEndPoint(String conversationId) {
		return StringUtils.format("/conversations/%s/sdkapprelease", conversationId);
	}

	@Override
	public HttpRequestMethod getHttpRequestMethod() {
		return HttpRequestMethod.PUT;
	}

	@Override
	public String getHttpRequestContentType() {
		return "application/json";
	}

	//endregion

	//region Sdk getters/setters
	public String getVersion() {
		return getString(KEY_VERSION);
	}

	public void setVersion(String version) {
		put(KEY_VERSION, version);
	}

	public String getProgrammingLanguage() {
		return getString(KEY_PROGRAMMING_LANGUAGE);
	}

	public void setProgrammingLanguage(String programmingLanguage) {
		put(KEY_PROGRAMMING_LANGUAGE, programmingLanguage);
	}

	public String getAuthorName() {
		return getString(KEY_AUTHOR_NAME);
	}

	public void setAuthorName(String authorName) {
		put(KEY_AUTHOR_NAME, authorName);
	}

	public String getAuthorEmail() {
		return getString(KEY_AUTHOR_EMAIL);
	}

	public void setAuthorEmail(String authorEmail) {
		put(KEY_AUTHOR_EMAIL, authorEmail);
	}

	public String getPlatform() {
		return getString(KEY_PLATFORM);
	}

	public void setPlatform(String platform) {
		put(KEY_PLATFORM, platform);
	}

	public String getDistribution() {
		return getString(KEY_DISTRIBUTION);
	}

	public void setDistribution(String distribution) {
		put(KEY_DISTRIBUTION, distribution);
	}

	public String getDistributionVersion() {
		return getString(KEY_DISTRIBUTION_VERSION);
	}

	public void setDistributionVersion(String distributionVersion) {
		put(KEY_DISTRIBUTION_VERSION, distributionVersion);
	}
	//endregion

	//region AppRelease getters/setters

	public String getType() {
		return getString(KEY_TYPE);
	}

	public void setType(String type) {
		put(KEY_TYPE, type);
	}

	public String getVersionName() {
		return getString(KEY_VERSION_NAME);
	}

	public void setVersionName(String versionName) {
		put(KEY_VERSION_NAME, versionName);
	}

	public int getVersionCode() {
		return getInt(KEY_VERSION_CODE, -1);
	}

	public void setVersionCode(int versionCode) {
		put(KEY_VERSION_CODE, versionCode);
	}

	public String getIdentifier() {
		return getString(KEY_IDENTIFIER);
	}

	public void setIdentifier(String identifier) {
		put(KEY_IDENTIFIER, identifier);
	}

	public String getTargetSdkVersion() {
		return getString(KEY_TARGET_SDK_VERSION);
	}

	public void setTargetSdkVersion(String targetSdkVersion) {
		put(KEY_TARGET_SDK_VERSION, targetSdkVersion);
	}

	public String getAppStore() {
		return getString(KEY_APP_STORE);
	}

	public void setAppStore(String appStore) {
		put(KEY_APP_STORE, appStore);
	}

	// Flag for whether the apptentive is inheriting styles from the host app
	public boolean getInheritStyle() {
		return getBoolean(KEY_STYLE_INHERIT);
	}

	public void setInheritStyle(boolean inheritStyle) {
		put(KEY_STYLE_INHERIT, inheritStyle);
	}

	// Flag for whether the app is overriding any Apptentive Styles
	public boolean getOverrideStyle() {
		return getBoolean(KEY_STYLE_OVERRIDE);
	}

	public void setOverrideStyle(boolean overrideStyle) {
		put(KEY_STYLE_OVERRIDE, overrideStyle);
	}

	public boolean getDebug() {
		return getBoolean(KEY_DEBUG);
	}

	public void setDebug(boolean debug) {
		put(KEY_DEBUG, debug);
	}

	public static AppReleasePayload generateCurrentAppRelease(Context context) {

		AppReleasePayload appRelease = new AppReleasePayload();

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

		appRelease.setType("android");
		appRelease.setVersionName(currentVersionName);
		appRelease.setIdentifier(appPackageName);
		appRelease.setVersionCode(currentVersionCode);
		appRelease.setTargetSdkVersion(String.valueOf(targetSdkVersion));
		appRelease.setAppStore(Util.getInstallerPackageName(context));
		appRelease.setInheritStyle(ApptentiveInternal.getInstance().isAppUsingAppCompatTheme());
		appRelease.setOverrideStyle(themeOverrideResId != 0);
		appRelease.setDebug(isAppDebuggable);

		return appRelease;
	}
	//endregion
}
