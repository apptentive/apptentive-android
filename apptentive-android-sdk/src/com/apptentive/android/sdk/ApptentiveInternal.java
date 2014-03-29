/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.module.engagement.EngagementModule;

/**
 * This class contains only internal methods. These methods should not be access directly by the host app.
 *
 * @author Sky Kelsey
 */
public class ApptentiveInternal {

	/**
	 * Internal use only.
	 */
	public static void onAppLaunch(final Activity activity) {
		EngagementModule.engageInternal(activity, Event.EventLabel.app__launch.getLabelName());
	}
}
