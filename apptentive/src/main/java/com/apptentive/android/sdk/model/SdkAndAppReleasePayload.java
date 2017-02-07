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
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;

/**
 * A combined payload of {@link Sdk} and {@link AppRelease} payloads.
 *
 * This class effectively contains the source code from both {@link Sdk}
 * and {@link AppRelease} payloads (which still kept for backward compatibility
 * purposes).
 */
public class SdkAndAppReleasePayload extends Payload {

	//region Sdk payload keys
	private static final String KEY_VERSION = "version";
	private static final String KEY_PROGRAMMING_LANGUAGE = "programming_language";
	private static final String KEY_AUTHOR_NAME = "author_name";
	private static final String KEY_AUTHOR_EMAIL = "author_email";
	private static final String KEY_PLATFORM = "platform";
	private static final String KEY_DISTRIBUTION = "distribution";
	private static final String KEY_DISTRIBUTION_VERSION = "distribution_version";
	//endregion

	//region AppRelease keys
	private static final String KEY_TYPE = "type";
	private static final String KEY_VERSION_NAME = "version_name";
	private static final String KEY_VERSION_CODE = "version_code";
	private static final String KEY_IDENTIFIER = "identifier";
	private static final String KEY_TARGET_SDK_VERSION = "target_sdk_version";
	private static final String KEY_APP_STORE = "app_store";
	private static final String KEY_STYLE_INHERIT = "inheriting_styles";
	private static final String KEY_STYLE_OVERRIDE = "overriding_styles";
	private static final String KEY_DEBUG = "debug";
	//endregion

	public static SdkAndAppReleasePayload fromJson(String json) {
		try {
			return new SdkAndAppReleasePayload(json);
		} catch (JSONException e) {
			ApptentiveLog.v("Error parsing json as SdkAndAppReleasePayload: %s", e, json);
		} catch (IllegalArgumentException e) {
			// Unknown unknown #rumsfeld
		}
		return null;
	}

	public SdkAndAppReleasePayload(String json) throws JSONException {
		super(json);
	}

	public SdkAndAppReleasePayload() {
		super();
	}

	//region Inheritance
	public void initBaseType() {
		setBaseType(BaseType.sdk_and_app_release);
	}
	//endregion

	//region Sdk getters/setters
	public String getVersion() {
		try {
			if(!isNull(KEY_VERSION)) {
				return getString(KEY_VERSION);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public void setVersion(String version) {
		try {
			put(KEY_VERSION, version);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to Sdk.", KEY_VERSION);
		}
	}

	public String getProgrammingLanguage() {
		try {
			if(!isNull(KEY_PROGRAMMING_LANGUAGE)) {
				return getString(KEY_PROGRAMMING_LANGUAGE);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public void setProgrammingLanguage(String programmingLanguage) {
		try {
			put(KEY_PROGRAMMING_LANGUAGE, programmingLanguage);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to Sdk.", KEY_PROGRAMMING_LANGUAGE);
		}
	}

	public String getAuthorName() {
		try {
			if(!isNull(KEY_AUTHOR_NAME)) {
				return getString(KEY_AUTHOR_NAME);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public void setAuthorName(String authorName) {
		try {
			put(KEY_AUTHOR_NAME, authorName);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to Sdk.", KEY_AUTHOR_NAME);
		}
	}

	public String getAuthorEmail() {
		try {
			if(!isNull(KEY_AUTHOR_EMAIL)) {
				return getString(KEY_AUTHOR_EMAIL);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public void setAuthorEmail(String authorEmail) {
		try {
			put(KEY_AUTHOR_EMAIL, authorEmail);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to Sdk.", KEY_AUTHOR_EMAIL);
		}
	}

	public String getPlatform() {
		try {
			if(!isNull(KEY_PLATFORM)) {
				return getString(KEY_PLATFORM);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public void setPlatform(String platform) {
		try {
			put(KEY_PLATFORM, platform);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to Sdk.", KEY_PLATFORM);
		}
	}

	public String getDistribution() {
		try {
			if(!isNull(KEY_DISTRIBUTION)) {
				return getString(KEY_DISTRIBUTION);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public void setDistribution(String distribution) {
		try {
			put(KEY_DISTRIBUTION, distribution);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to Sdk.", KEY_DISTRIBUTION);
		}
	}

	public String getDistributionVersion() {
		try {
			if(!isNull(KEY_DISTRIBUTION_VERSION)) {
				return getString(KEY_DISTRIBUTION_VERSION);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public void setDistributionVersion(String distributionVersion) {
		try {
			put(KEY_DISTRIBUTION_VERSION, distributionVersion);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to Sdk.", KEY_DISTRIBUTION_VERSION);
		}
	}
	//endregion

	//region AppRelease getters/setters
	public String getType() {
		if (!isNull(KEY_TYPE)) {
			return optString(KEY_TYPE, null);
		}
		return null;
	}

	public void setType(String type) {
		try {
			put(KEY_TYPE, type);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to AppRelease.", KEY_TYPE);
		}
	}

	public String getVersionName() {
		if (!isNull(KEY_VERSION_NAME)) {
			return optString(KEY_VERSION_NAME, null);
		}
		return null;
	}

	public void setVersionName(String versionName) {
		try {
			put(KEY_VERSION_NAME, versionName);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to AppRelease.", KEY_VERSION_NAME);
		}
	}

	public int getVersionCode() {
		if (!isNull(KEY_VERSION_CODE)) {
			return optInt(KEY_VERSION_CODE, -1);
		}
		return -1;
	}

	public void setVersionCode(int versionCode) {
		try {
			put(KEY_VERSION_CODE, versionCode);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to AppRelease.", KEY_VERSION_CODE);
		}
	}

	public String getIdentifier() {
		if (!isNull(KEY_IDENTIFIER)) {
			return optString(KEY_IDENTIFIER, null);
		}
		return null;
	}

	public void setIdentifier(String identifier) {
		try {
			put(KEY_IDENTIFIER, identifier);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to AppRelease.", KEY_IDENTIFIER);
		}
	}

	public String getTargetSdkVersion() {
		if (!isNull(KEY_TARGET_SDK_VERSION)) {
			return optString(KEY_TARGET_SDK_VERSION);
		}
		return null;
	}

	public void setTargetSdkVersion(String targetSdkVersion) {
		try {
			put(KEY_TARGET_SDK_VERSION, targetSdkVersion);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to AppRelease.", KEY_TARGET_SDK_VERSION);
		}
	}

	public String getAppStore() {
		if (!isNull(KEY_APP_STORE)) {
			return optString(KEY_APP_STORE, null);
		}
		return null;
	}

	public void setAppStore(String appStore) {
		try {
			put(KEY_APP_STORE, appStore);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to AppRelease.", KEY_APP_STORE);
		}
	}

	// Flag for whether the apptentive is inheriting styles from the host app
	public boolean getInheritStyle() {
		return optBoolean(KEY_STYLE_INHERIT);
	}

	public void setInheritStyle(boolean inheritStyle) {
		try {
			put(KEY_STYLE_INHERIT, inheritStyle);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to AppRelease.", KEY_STYLE_INHERIT);
		}
	}

	// Flag for whether the app is overriding any Apptentive Styles
	public boolean getOverrideStyle() {
		return optBoolean(KEY_STYLE_OVERRIDE);
	}

	public void setOverrideStyle(boolean overrideStyle) {
		try {
			put(KEY_STYLE_OVERRIDE, overrideStyle);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to AppRelease.", KEY_STYLE_OVERRIDE);
		}
	}

	public boolean getDebug() {
		return optBoolean(KEY_DEBUG);
	}

	public void setDebug(boolean debug) {
		try {
			put(KEY_DEBUG, debug);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to AppRelease.", KEY_DEBUG);
		}
	}

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
