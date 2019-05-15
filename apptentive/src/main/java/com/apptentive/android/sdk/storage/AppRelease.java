/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import java.io.Serializable;

public class AppRelease implements Serializable {
	private static final long serialVersionUID = 8789914596082013978L;
	private String appStore;
	private boolean debug;
	private String identifier;
	private boolean inheritStyle;
	private boolean overrideStyle;
	private String targetSdkVersion;
	private String type;
	private int versionCode;
	private String versionName;

	//region Getters & Setters

	public String getAppStore() {
		return appStore;
	}

	public void setAppStore(String appStore) {
		this.appStore = appStore;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public boolean isInheritStyle() {
		return inheritStyle;
	}

	public void setInheritStyle(boolean inheritStyle) {
		this.inheritStyle = inheritStyle;
	}

	public boolean isOverrideStyle() {
		return overrideStyle;
	}

	public void setOverrideStyle(boolean overrideStyle) {
		this.overrideStyle = overrideStyle;
	}

	public String getTargetSdkVersion() {
		return targetSdkVersion;
	}

	public void setTargetSdkVersion(String targetSdkVersion) {
		this.targetSdkVersion = targetSdkVersion;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	//endregion
}
