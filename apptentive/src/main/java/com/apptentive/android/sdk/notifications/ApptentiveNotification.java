/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.notifications;

import com.apptentive.android.sdk.util.StringUtils;

import java.util.Map;

/**
 * {@link ApptentiveNotification} objects encapsulate information so that it can be broadcast to
 * other objects by {@link ApptentiveNotificationCenter}. An {@link ApptentiveNotification} object
 * contains a name and an optional dictionary. The name is a tag identifying the notification.
 * The dictionary stores other related objects, if any.
 * {@link ApptentiveNotification} objects are immutable objects.
 */
public class ApptentiveNotification {
	private final String name;
	private final Map<String, Object> userInfo;

	public ApptentiveNotification(String name, Map<String, Object> userInfo) {
		if (StringUtils.isNullOrEmpty(name)) {
			throw new IllegalArgumentException("Name is null or empty");
		}
		this.name = name;
		this.userInfo = userInfo;
	}

	public String getName() {
		return name;
	}

	public boolean hasName(String name) {
		return StringUtils.equal(this.name, name);
	}

	public Map<String, Object> getUserInfo() {
		return userInfo;
	}

	@Override
	public String toString() {
		return String.format("name=%s userInfo={%s}", name, StringUtils.toString(userInfo));
	}
}
