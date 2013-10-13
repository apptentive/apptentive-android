/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Log;
import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class AppRelease extends Payload {

	private static final String KEY_VERSION = "version";
	private static final String KEY_BUILD_NUMBER = "build_number";
	private static final String KEY_TARGET_SDK_VERSION = "target_sdk_version";

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
		}
		return null;
	}

	public void setVersion(String version) {
		try {
			put(KEY_VERSION, version);
		} catch (JSONException e) {
			Log.w("Error adding %s to AppRelease.", KEY_VERSION);
		}
	}

	public String getBuildNumber() {
		try {
			if(!isNull(KEY_BUILD_NUMBER)) {
				return getString(KEY_BUILD_NUMBER);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public void setBuildNumber(String buildNumber) {
		try {
			put(KEY_BUILD_NUMBER, buildNumber);
		} catch (JSONException e) {
			Log.w("Error adding %s to AppRelease.", KEY_BUILD_NUMBER);
		}
	}

	public String getTargetSdkVersion() {
		try {
			if(!isNull(KEY_TARGET_SDK_VERSION)) {
				return getString(KEY_TARGET_SDK_VERSION);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public void setTargetSdkVersion(String targetSdkVersion) {
		try {
			put(KEY_TARGET_SDK_VERSION, targetSdkVersion);
		} catch (JSONException e) {
			Log.w("Error adding %s to AppRelease.", KEY_TARGET_SDK_VERSION);
		}
	}

}
