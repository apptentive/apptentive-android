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

import com.apptentive.android.sdk.module.ActivityContent;

/**
 * @author Sky Kelsey
 */
public class MessageCenterErrorActivityContent extends ActivityContent {
	private Context context;
	private MessageCenterErrorView messageCenterErrorView;

	@Override
	public void onCreate(Activity activity, Bundle onSavedInstanceState) {
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

	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {

	}

	@Override
	public boolean onBackPressed(Activity activity) {
		return true;
	}
}
