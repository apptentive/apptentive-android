/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.migration.v4_0_0;

import com.apptentive.android.sdk.ApptentiveLog;

import org.json.JSONException;
import org.json.JSONObject;

import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class AppRelease extends JSONObject {

	private static final String KEY_TYPE = "type";
	private static final String KEY_VERSION_NAME = "version_name";
	private static final String KEY_VERSION_CODE = "version_code";
	private static final String KEY_IDENTIFIER = "identifier";
	private static final String KEY_TARGET_SDK_VERSION = "target_sdk_version";
	private static final String KEY_APP_STORE = "app_store";
	private static final String KEY_STYLE_INHERIT = "inheriting_styles";
	private static final String KEY_STYLE_OVERRIDE = "overriding_styles";
	private static final String KEY_DEBUG = "debug";

	public AppRelease(String json) throws JSONException {
		super(json);
	}

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
			ApptentiveLog.w(CONVERSATION, "Error adding %s to AppRelease.", KEY_TYPE);
			logException(e);
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
			ApptentiveLog.w(CONVERSATION, "Error adding %s to AppRelease.", KEY_VERSION_NAME);
			logException(e);
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
			ApptentiveLog.w(CONVERSATION, "Error adding %s to AppRelease.", KEY_VERSION_CODE);
			logException(e);
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
			ApptentiveLog.w(CONVERSATION, "Error adding %s to AppRelease.", KEY_IDENTIFIER);
			logException(e);
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
			ApptentiveLog.w(CONVERSATION, "Error adding %s to AppRelease.", KEY_TARGET_SDK_VERSION);
			logException(e);
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
			ApptentiveLog.w(CONVERSATION, "Error adding %s to AppRelease.", KEY_APP_STORE);
			logException(e);
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
			ApptentiveLog.w(CONVERSATION, "Error adding %s to AppRelease.", KEY_STYLE_INHERIT);
			logException(e);
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
			ApptentiveLog.w(CONVERSATION, "Error adding %s to AppRelease.", KEY_STYLE_OVERRIDE);
			logException(e);
		}
	}

	public boolean getDebug() {
		return optBoolean(KEY_DEBUG);
	}

	public void setDebug(boolean debug) {
		try {
			put(KEY_DEBUG, debug);
		} catch (JSONException e) {
			ApptentiveLog.w(CONVERSATION, "Error adding %s to AppRelease.", KEY_DEBUG);
			logException(e);
		}
	}
}