/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.ExtendedData;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.util.Constants;

/**
 * @author Sky Kelsey
 */
public class MessageCenterErrorView extends FrameLayout {

	public MessageCenterErrorView(final Activity activity) {
		super(activity.getApplicationContext());

		LayoutInflater inflater = activity.getLayoutInflater();
		inflater.inflate(R.layout.apptentive_message_center_error, this);

		ImageButton back = (ImageButton) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cleanup(activity);
				activity.finish();
			}
		});

		if (wasLastAttemptServerError(getContext())) {
			((ImageView) findViewById(R.id.icon)).setImageResource(R.drawable.apptentive_icon_server_error);
			((TextView) findViewById(R.id.message)).setText(R.string.apptentive_message_center_server_error);
		} else {
			((ImageView) findViewById(R.id.icon)).setImageResource(R.drawable.apptentive_icon_no_connection);
			((TextView) findViewById(R.id.message)).setText(R.string.apptentive_message_center_no_connection);
		}

	}

	public static boolean wasLastAttemptServerError(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(Constants.PREF_KEY_MESSAGE_CENTER_SERVER_ERROR_LAST_ATTEMPT, false);
	}

	public void cleanup(Activity activity) {
		EngagementModule.engage(activity, "com.apptentive", "MessageCenter", null, MessageCenterErrorActivityContent.EVENT_NAME_NO_INTERACTION_CLOSE, null, null, (ExtendedData[]) null);
	}
}
