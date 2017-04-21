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

import org.json.JSONException;

/**
 * A combined payload of {@link SdkPayload} and {@link AppReleasePayload} payloads.
 *
 * This class effectively contains the source code from both {@link SdkPayload}
 * and {@link AppReleasePayload} payloads (which still kept for backward compatibility
 * purposes).
 */
public class SdkAndAppReleasePayload extends JsonPayload {

	private final SdkPayload sdk;
	private final AppReleasePayload appRelease;

	public SdkAndAppReleasePayload() {
		sdk = new SdkPayload();
		appRelease = new AppReleasePayload();

		// TODO: a better solution
		put("sdk", sdk.getJsonObject());
		put("app_release", appRelease.getJsonObject());
	}

	public SdkAndAppReleasePayload(String json) throws JSONException {
		super(json);

		sdk = new SdkPayload(getJSONObject("sdk").toString());
		appRelease = new AppReleasePayload(getJSONObject("app_release").toString());
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
		return sdk.getVersion();
	}

	public void setVersion(String version) {
		sdk.setVersion(version);
	}

	public String getProgrammingLanguage() {
		return sdk.getProgrammingLanguage();
	}

	public void setProgrammingLanguage(String programmingLanguage) {
		sdk.setProgrammingLanguage(programmingLanguage);
	}

	public String getAuthorName() {
		return sdk.getAuthorName();
	}

	public void setAuthorName(String authorName) {
		sdk.setAuthorName(authorName);
	}

	public String getAuthorEmail() {
		return sdk.getAuthorEmail();
	}

	public void setAuthorEmail(String authorEmail) {
		sdk.setAuthorEmail(authorEmail);
	}

	public String getPlatform() {
		return sdk.getPlatform();
	}

	public void setPlatform(String platform) {
		sdk.setPlatform(platform);
	}

	public String getDistribution() {
		return sdk.getDistribution();
	}

	public void setDistribution(String distribution) {
		sdk.setDistribution(distribution);
	}

	public String getDistributionVersion() {
		return sdk.getDistributionVersion();
	}

	public void setDistributionVersion(String distributionVersion) {
		sdk.setDistributionVersion(distributionVersion);
	}
	//endregion

	//region AppRelease getters/setters
	public String getType() {
		return appRelease.getType();
	}

	public void setType(String type) {
		appRelease.setType(type);
	}

	public String getVersionName() {
		return appRelease.getVersionName();
	}

	public void setVersionName(String versionName) {
		appRelease.setVersionName(versionName);
	}

	public int getVersionCode() {
		return appRelease.getVersionCode();
	}

	public void setVersionCode(int versionCode) {
		appRelease.setVersionCode(versionCode);
	}

	public String getIdentifier() {
		return appRelease.getIdentifier();
	}

	public void setIdentifier(String identifier) {
		appRelease.setIdentifier(identifier);
	}

	public String getTargetSdkVersion() {
		return appRelease.getTargetSdkVersion();
	}

	public void setTargetSdkVersion(String targetSdkVersion) {
		appRelease.setTargetSdkVersion(targetSdkVersion);
	}

	public String getAppStore() {
		return appRelease.getAppStore();
	}

	public void setAppStore(String appStore) {
		appRelease.setAppStore(appStore);
	}

	// Flag for whether the apptentive is inheriting styles from the host app
	public boolean getInheritStyle() {
		return appRelease.getInheritStyle();
	}

	public void setInheritStyle(boolean inheritStyle) {
		appRelease.setInheritStyle(inheritStyle);
	}

	// Flag for whether the app is overriding any Apptentive Styles
	public boolean getOverrideStyle() {
		return appRelease.getOverrideStyle();
	}

	public void setOverrideStyle(boolean overrideStyle) {
		appRelease.setOverrideStyle(overrideStyle);
	}

	public boolean getDebug() {
		return appRelease.getDebug();
	}

	public void setDebug(boolean debug) {
		appRelease.setDebug(debug);
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
