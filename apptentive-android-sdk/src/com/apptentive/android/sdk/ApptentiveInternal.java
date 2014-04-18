/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.impl.GooglePlayRatingProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains only internal methods. These methods should not be access directly by the host app.
 *
 * @author Sky Kelsey
 */
public class ApptentiveInternal {

	private static IRatingProvider ratingProvider;
	private static Map<String, String> ratingProviderArgs;

	public static final String PUSH_ACTION = "action";

	public static enum PushAction {
		pmc,       // Present Message Center.
		unknown;   // Anything unknown will not be handled.

		public static PushAction parse(String name) {
			try {
				return PushAction.valueOf(name);
			} catch (IllegalArgumentException e) {
				Log.d("Error parsing unknown PushAction: " + name);
			}
			return unknown;
		}
	}

	public static void onAppLaunch(final Activity activity) {
		EngagementModule.engageInternal(activity, Event.EventLabel.app__launch.getLabelName());
	}

	public static IRatingProvider getRatingProvider() {
		if (ratingProvider == null) {
			ratingProvider = new GooglePlayRatingProvider();
		}
		return ratingProvider;
	}

	public static void setRatingProvider(IRatingProvider ratingProvider) {
		ApptentiveInternal.ratingProvider = ratingProvider;
	}

	public static Map<String, String> getRatingProviderArgs() {
		return ratingProviderArgs;
	}

	public static void putRatingProviderArg(String key, String value) {
		if (ratingProviderArgs == null) {
			ratingProviderArgs = new HashMap<String, String>();
		}
		ratingProviderArgs.put(key, value);
	}

}
