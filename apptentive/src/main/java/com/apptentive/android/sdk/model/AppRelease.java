/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveLog;
import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class AppRelease extends Payload {

	private static final String KEY_VERSION = "version";
	private static final String KEY_BUILD_NUMBER = "build_number";
	private static final String KEY_IDENTIFIER = "identifier";
	private static final String KEY_TARGET_SDK_VERSION = "target_sdk_version";
	private static final String KEY_APP_STORE = "app_store";
	private static final String KEY_STYLE_INHERIT = "inheriting_styles";
	private static final String KEY_STYLE_OVERRIDE = "overriding_styles";

	public AppRelease(String json) throws JSONException {
		super(json);
	}

	public AppRelease() {
		super();
	}

	public void initBaseType() {
		setBaseType(BaseType.app_release);
	}

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
			ApptentiveLog.w("Error adding %s to AppRelease.", KEY_VERSION);
		}
	}

	public String getBuildNumber() {
		try {
			if(!isNull(KEY_BUILD_NUMBER)) {
				return getString(KEY_BUILD_NUMBER);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public void setBuildNumber(String buildNumber) {
		try {
			put(KEY_BUILD_NUMBER, buildNumber);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to AppRelease.", KEY_BUILD_NUMBER);
		}
	}

	public String getIdentifier() {
		try {
			if(!isNull(KEY_IDENTIFIER)) {
				return getString(KEY_IDENTIFIER);
			}
		} catch (JSONException e) {
			// Ignore
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
		try {
			if(!isNull(KEY_TARGET_SDK_VERSION)) {
				return getString(KEY_TARGET_SDK_VERSION);
			}
		} catch (JSONException e) {
			// Ignore
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
		try {
			if(!isNull(KEY_APP_STORE)) {
				return getString(KEY_APP_STORE);
			}
		} catch (JSONException e) {
			// Ignore
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

	public void setInheritStyle(boolean bval) {
		try {
			put(KEY_STYLE_INHERIT, bval);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to AppRelease.", KEY_STYLE_INHERIT);
		}
	}

	// Flag for whether the app is overriding any Apptentive Styles
	public boolean getOverrideStyle() {
		return optBoolean(KEY_STYLE_OVERRIDE);
	}

	public void setOverrideStyle(boolean bval) {
		try {
			put(KEY_STYLE_OVERRIDE, bval);
		} catch (JSONException e) {
			ApptentiveLog.w("Error adding %s to AppRelease.", KEY_STYLE_OVERRIDE);
		}
	}

}
