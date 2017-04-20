/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.network.HttpRequestMethod;

public class AppReleasePayload extends JsonPayload {

	private static final String KEY_TYPE = "type";
	private static final String KEY_VERSION_NAME = "version_name";
	private static final String KEY_VERSION_CODE = "version_code";
	private static final String KEY_IDENTIFIER = "identifier";
	private static final String KEY_TARGET_SDK_VERSION = "target_sdk_version";
	private static final String KEY_APP_STORE = "app_store";
	private static final String KEY_STYLE_INHERIT = "inheriting_styles";
	private static final String KEY_STYLE_OVERRIDE = "overriding_styles";
	private static final String KEY_DEBUG = "debug";

	//region Http-request

	@Override
	public String getHttpEndPoint() {
		throw new RuntimeException(getClass().getName() +  " is deprecated"); // FIXME: find a better approach
	}

	@Override
	public HttpRequestMethod getHttpRequestMethod() {
		throw new RuntimeException(getClass().getName() +  " is deprecated"); // FIXME: find a better approach
	}

	@Override
	public String getHttpRequestContentType() {
		throw new RuntimeException(getClass().getName() +  " is deprecated"); // FIXME: find a better approach
	}

	//endregion

	public void initPayloadType() {
		setPayloadType(PayloadType.app_release);
	}

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
		if (!isNull(KEY_VERSION_CODE)) {
			return getInt(KEY_VERSION_CODE);
		}
		return -1;
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
}
