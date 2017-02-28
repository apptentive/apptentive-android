/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

public class ApptentiveNotifications {

	/**
	 * Sent if a new activity is started.
	 */
	public static final String NOTIFICATION_ACTIVITY_STARTED = "NOTIFICATION_ACTIVITY_STARTED"; // { activityClass : Class<? extend Activity> }
	public static final String NOTIFICATION_ACTIVITY_STARTED_KEY_ACTIVITY_CLASS = "activityClass";

	/**
	 * Sent if user requested to close all interactions.
	 */
	public static final String NOTIFICATION_INTERACTIONS_SHOULD_DISMISS = "NOTIFICATION_INTERACTIONS_SHOULD_DISMISS";
}
