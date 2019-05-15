/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores a record of an event occurring.
 */
public class EventRecord implements Serializable {
	private static final long serialVersionUID = 1485363290506105166L;
	private double last;
	private long total;
	private Map<Integer, Long> versionCodes;
	private Map<String, Long> versionNames;

	public EventRecord() {
		last = 0D;
		total = 0L;
		versionCodes = new HashMap<Integer, Long>();
		versionNames = new HashMap<String, Long>();
	}

	//region Getters & Setters

	public double getLast() {
		return last;
	}

	public long getTotal() {
		return total;
	}

	//endregion

	/**
	 * Initializes an event record or updates it with a subsequent event.
	 * @param timestamp The timestamp in seconds at which and Event occurred.
	 * @param versionName The Android versionName of the app when the event occurred.
	 * @param versionCode The Android versionCode of the app when the event occurred.
	 */
	public void update(double timestamp, String versionName, Integer versionCode) {
		last = timestamp;
		total++;
		Long countForVersionName = versionNames.get(versionName);
		if (countForVersionName == null) {
			countForVersionName = 0L;
		}
		Long countForVersionCode = versionCodes.get(versionCode);
		if (countForVersionCode == null) {
			countForVersionCode = 0L;
		}
		versionNames.put(versionName, countForVersionName + 1);
		versionCodes.put(versionCode, countForVersionCode + 1);
	}

	public Long getCountForVersionName(String versionName) {
		Long count = versionNames.get(versionName);
		if (count != null) {
			return count;
		}
		return 0L;
	}

	public Long getCountForVersionCode(Integer versionCode) {
		Long count = versionCodes.get(versionCode);
		if (count != null) {
			return count;
		}
		return 0L;
	}

	/**
	 * Only access directly for migration.
	 */
	public void setLast(double last) {
		this.last = last;
	}

	/**
	 * Only access directly for migration.
	 */
	public void setTotal(long total) {
		this.total = total;
	}

	/**
	 * Only access directly for migration.
	 */
	public void setVersionCodes(Map<Integer, Long> versionCodes) {
		this.versionCodes = versionCodes;
	}

	/**
	 * Only access directly for migration.
	 */
	public void setVersionNames(Map<String, Long> versionNames) {
		this.versionNames = versionNames;
	}

	@Override
	public String toString() {
		return "EventRecord{" +
			"last=" + last +
			", total=" + total +
			", versionNames=" + versionNames +
			", versionCodes=" + versionCodes +
			'}';
	}
}
