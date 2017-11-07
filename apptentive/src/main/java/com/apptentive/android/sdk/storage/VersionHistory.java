/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.util.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VersionHistory implements Saveable {

	private static final long serialVersionUID = 1L;

	/**
	 * An ordered list of version history. Older versions are first, new versions are added to the end.
	 */
	private List<VersionHistoryItem> versionHistoryItems;

	public VersionHistory() {
		versionHistoryItems = new ArrayList<>();
	}

	//region Listeners
	private transient DataChangedListener listener;

	@Override
	public void setDataChangedListener(DataChangedListener listener) {
		this.listener = listener;
	}

	@Override
	public void notifyDataChanged() {
		if (listener != null) {
			listener.onDataChanged();
		}
	}

	//endregion

	public void updateVersionHistory(double timestamp, Integer newVersionCode, String newVersionName) {
		boolean exists = false;
		for (VersionHistoryItem item : versionHistoryItems) {
			if (item.getVersionCode() == newVersionCode && item.getVersionName().equals(newVersionName)) {
				exists = true;
				break;
			}
		}
		if (!exists) {
			VersionHistoryItem newVersionHistoryItem = new VersionHistoryItem(timestamp, newVersionCode, newVersionName);
			versionHistoryItems.add(newVersionHistoryItem);
			notifyDataChanged();
		}
	}

	/**
	 * Returns the timestamp at the first install of this app that Apptentive was aware of.
	 */
	public Apptentive.DateTime getTimeAtInstallTotal() {
		// Simply return the first item's timestamp, if there is one.
		if (versionHistoryItems.size() > 0) {
			return new Apptentive.DateTime(versionHistoryItems.get(0).getTimestamp());
		}
		return new Apptentive.DateTime(Util.currentTimeSeconds());
	}

	/**
	 * Returns the timestamp at the first install of the current versionCode of this app that Apptentive was aware of.
	 */
	public Apptentive.DateTime getTimeAtInstallForVersionCode(int versionCode) {
		for (VersionHistoryItem item : versionHistoryItems) {
			if (item.getVersionCode() == versionCode) {
				return new Apptentive.DateTime(item.getTimestamp());
			}
		}
		return new Apptentive.DateTime(Util.currentTimeSeconds());
	}

	/**
	 * Returns the timestamp at the first install of the current versionName of this app that Apptentive was aware of.
	 */
	public Apptentive.DateTime getTimeAtInstallForVersionName(String versionName) {
		for (VersionHistoryItem item : versionHistoryItems) {
			Apptentive.Version entryVersionName = new Apptentive.Version();
			Apptentive.Version currentVersionName = new Apptentive.Version();
			entryVersionName.setVersion(item.getVersionName());
			currentVersionName.setVersion(versionName);
			if (entryVersionName.equals(currentVersionName)) {
				return new Apptentive.DateTime(item.getTimestamp());
			}
		}
		return new Apptentive.DateTime(Util.currentTimeSeconds());
	}

	/**
	 * Returns true if the current versionCode is not the first version or build that we have seen. Basically, it just
	 * looks for two or more versionCodes.
	 *
	 * @return True if this is not the first versionCode of the app we've seen.
	 */
	public boolean isUpdateForVersionCode() {
		Set<Integer> uniques = new HashSet<Integer>();
		for (VersionHistoryItem item : versionHistoryItems) {
			uniques.add(item.getVersionCode());
		}
		return uniques.size() > 1;
	}

	/**
	 * Returns true if the current versionName is not the first version or build that we have seen. Basically, it just
	 * looks for two or more versionNames.
	 *
	 * @return True if this is not the first versionName of the app we've seen.
	 */
	public boolean isUpdateForVersionName() {
		Set<String> uniques = new HashSet<String>();
		for (VersionHistoryItem item : versionHistoryItems) {
			uniques.add(item.getVersionName());
		}
		return uniques.size() > 1;
	}

	public VersionHistoryItem getLastVersionSeen() {
		if (!versionHistoryItems.isEmpty()) {
			return versionHistoryItems.get(versionHistoryItems.size() - 1);
		}
		return null;
	}

	public synchronized void clear() {
		versionHistoryItems.clear();
		notifyDataChanged();
	}
}
