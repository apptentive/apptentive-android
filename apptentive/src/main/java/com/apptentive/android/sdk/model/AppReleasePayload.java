/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.network.HttpRequestMethod;

import org.json.JSONException;

public class AppReleasePayload extends JsonPayload {

	public static final String KEY = "app_release";

	private static final String KEY_TYPE = "type";
	private static final String KEY_VERSION_NAME = "version_name";
	private static final String KEY_VERSION_CODE = "version_code";
	private static final String KEY_IDENTIFIER = "identifier";
	private static final String KEY_TARGET_SDK_VERSION = "target_sdk_version";
	private static final String KEY_APP_STORE = "app_store";
	private static final String KEY_STYLE_INHERIT = "inheriting_styles";
	private static final String KEY_STYLE_OVERRIDE = "overriding_styles";
	private static final String KEY_DEBUG = "debug";

	public AppReleasePayload() {
		super(PayloadType.app_release);
	}

	public AppReleasePayload(String json) throws JSONException {
		super(PayloadType.app_release, json);
	}

	//region Http-request

	@Override
	public String getHttpEndPoint(String conversationId) {
		throw new RuntimeException(getClass().getName() +  " is deprecated"); // TODO: find a better approach
	}

	@Override
	public HttpRequestMethod getHttpRequestMethod() {
		throw new RuntimeException(getClass().getName() +  " is deprecated"); // TODO: find a better approach
	}

	@Override
	public String getHttpRequestContentType() {
		throw new RuntimeException(getClass().getName() +  " is deprecated"); // TODO: find a better approach
	}

	//endregion

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
}
