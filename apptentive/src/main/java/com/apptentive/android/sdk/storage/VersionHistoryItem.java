/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import java.io.Serializable;

public class VersionHistoryItem implements Serializable {
	private static final long serialVersionUID = 1730491670319107507L;
	private double timestamp;
	private int versionCode;
	private String versionName;

	public VersionHistoryItem(double timestamp, int versionCode, String versionName) {
		this.timestamp = timestamp;
		this.versionCode = versionCode;
		this.versionName = versionName;
	}

	//region Getters & Setters

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

	public double getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(double timestamp) {
		this.timestamp = timestamp;
	}

	//endregion
}
