/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import android.content.Context;
import android.content.Intent;

import com.apptentive.android.sdk.ViewActivity;
import com.apptentive.android.sdk.module.ActivityContent;

import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class MessageCenterInteraction extends Interaction {

	// The server guarantees that an instance of this Interaction will be targetted to the following internal event name.
	public static final String DEFAULT_INTERNAL_EVENT_NAME = "show_message_center";

	public MessageCenterInteraction(String json) throws JSONException {
		super(json);
	}

	public static Intent generateMessageCenterErrorIntent(Context context) {
		Intent intent = new Intent();
		intent.setClass(context, ViewActivity.class);
		intent.putExtra(ActivityContent.KEY, ActivityContent.Type.MESSAGE_CENTER_ERROR.name());
		return intent;
	}
}
