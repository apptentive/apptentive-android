/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.util.StringUtils;

import static com.apptentive.android.sdk.ApptentiveLogTag.PAYLOADS;

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

	private static final String KEY_SDK_VERSION = "sdk_version";
	private static final String KEY_SDK_PROGRAMMING_LANGUAGE = "sdk_programming_language";
	private static final String KEY_SDK_AUTHOR_NAME = "sdk_author_name";
	private static final String KEY_SDK_AUTHOR_EMAIL = "sdk_author_email";
	private static final String KEY_SDK_PLATFORM = "sdk_platform";
	private static final String KEY_SDK_DISTRIBUTION = "sdk_distribution";
	private static final String KEY_SDK_DISTRIBUTION_VERSION = "sdk_distribution_version";

	public SdkAndAppReleasePayload() {
		super(PayloadType.sdk_and_app_release);
	}

	//region Http-request

	@Override
	public String getHttpEndPoint(String conversationId) {
		return StringUtils.format("/conversations/%s/app_release", conversationId);
	}

	//endregion

	//region Sdk getters/setters
	public String getVersion() {
		return optString(KEY_SDK_VERSION, null);
	}

	public void setVersion(String version) {
		put(KEY_SDK_VERSION, version);
	}

	public String getProgrammingLanguage() {
		return optString(KEY_SDK_PROGRAMMING_LANGUAGE, null);
	}

	public void setProgrammingLanguage(String programmingLanguage) {
		put(KEY_SDK_PROGRAMMING_LANGUAGE, programmingLanguage);
	}

	public String getAuthorName() {
		return optString(KEY_SDK_AUTHOR_NAME, null);
	}

	public void setAuthorName(String authorName) {
		put(KEY_SDK_AUTHOR_NAME, authorName);
	}

	public String getAuthorEmail() {
		return optString(KEY_SDK_AUTHOR_EMAIL, null);
	}

	public void setAuthorEmail(String authorEmail) {
		put(KEY_SDK_AUTHOR_EMAIL, authorEmail);
	}

	public String getPlatform() {
		return optString(KEY_SDK_PLATFORM, null);
	}

	public void setPlatform(String platform) {
		put(KEY_SDK_PLATFORM, platform);
	}

	public String getDistribution() {
		return optString(KEY_SDK_DISTRIBUTION, null);
	}

	public void setDistribution(String distribution) {
		put(KEY_SDK_DISTRIBUTION, distribution);
	}

	public String getDistributionVersion() {
		return optString(KEY_SDK_DISTRIBUTION_VERSION, null);
	}

	public void setDistributionVersion(String distributionVersion) {
		put(KEY_SDK_DISTRIBUTION_VERSION, distributionVersion);
	}
	//endregion

	//region AppRelease getters/setters

	public String getType() {
		return optString(KEY_TYPE, null);
	}

	public void setType(String type) {
		put(KEY_TYPE, type);
	}

	public String getVersionName() {
		return optString(KEY_VERSION_NAME, null);
	}

	public void setVersionName(String versionName) {
		put(KEY_VERSION_NAME, versionName);
	}

	public int getVersionCode() {
		return optInt(KEY_VERSION_CODE, -1);
	}

	public void setVersionCode(int versionCode) {
		put(KEY_VERSION_CODE, versionCode);
	}

	public String getIdentifier() {
		return optString(KEY_IDENTIFIER, null);
	}

	public void setIdentifier(String identifier) {
		put(KEY_IDENTIFIER, identifier);
	}

	public String getTargetSdkVersion() {
		return optString(KEY_TARGET_SDK_VERSION, null);
	}

	public void setTargetSdkVersion(String targetSdkVersion) {
		put(KEY_TARGET_SDK_VERSION, targetSdkVersion);
	}

	public String getAppStore() {
		return optString(KEY_APP_STORE, null);
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

	@Override
	protected String getJsonContainer() {
		return "app_release";
	}
	//endregion
}
