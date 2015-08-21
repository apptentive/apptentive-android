/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.ExtendedData;
import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.EngagementModule;

/**
 * @author Sky Kelsey
 */
public class MessageCenterErrorActivityContent extends ActivityContent {

	private static final String HAS_LAUNCHED = "has_launched";
	private static final String EVENT_NAME_NO_INTERACTION_NO_INTERNET = "no_interaction_no_internet";
	private static final String EVENT_NAME_NO_INTERACTION_ATTEMPTING = "no_interaction_attempting";
	public static final String EVENT_NAME_NO_INTERACTION_CLOSE = "no_interaction_close";

	private Context context;
	private MessageCenterErrorView messageCenterErrorView;

	protected boolean hasLaunched;

	@Override
	public void onCreate(Activity activity, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			hasLaunched = savedInstanceState.getBoolean(HAS_LAUNCHED);
		}
		if (!hasLaunched) {
			hasLaunched = true;
			if (MessageCenterErrorView.wasLastAttemptServerError(activity.getApplicationContext())) {
				EngagementModule.engage(activity, "com.apptentive", "MessageCenter", null, EVENT_NAME_NO_INTERACTION_ATTEMPTING, null, null, (ExtendedData[]) null);
			} else {
				EngagementModule.engage(activity, "com.apptentive", "MessageCenter", null, EVENT_NAME_NO_INTERACTION_NO_INTERNET, null, null, (ExtendedData[]) null);
			}
		}

		context = activity.getApplicationContext();
		messageCenterErrorView = new MessageCenterErrorView(activity);

		// TODO: Send metric?
		//MetricModule.sendMetric(context.getApplicationContext(), Event.EventLabel.message_center__launch);

		// Remove an existing MessageCenterView and replace it with this, if it exists.
		if (messageCenterErrorView.getParent() != null) {
			((ViewGroup) messageCenterErrorView.getParent()).removeView(messageCenterErrorView);
		}
		activity.setContentView(messageCenterErrorView);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(HAS_LAUNCHED, hasLaunched);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		hasLaunched = savedInstanceState.getBoolean(HAS_LAUNCHED, false);
	}


	@Override
	public boolean onBackPressed(Activity activity) {
		if (messageCenterErrorView != null) {
			messageCenterErrorView.cleanup(activity);
		}
		return true;
	}
}
