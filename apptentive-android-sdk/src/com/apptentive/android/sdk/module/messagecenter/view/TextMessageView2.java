/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.TextMessage;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey
 */
public class TextMessageView2 extends MessageView<TextMessage> {

	public TextMessageView2(Context context, TextMessage message) {
		super(context, message);

		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.apptentive_message_center_text_message_2, this);

		LinearLayout background = (LinearLayout) findViewById(R.id.apptentive_message_center_message_background);
		TextView sender = (TextView) findViewById(R.id.apptentive_message_sender);
		TextView timestamp = (TextView) findViewById(R.id.apptentive_message_timestamp);
		TextView text = (TextView) findViewById(R.id.apptentive_message_center_text_message_text);
		FrameLayout avatarFrameLeft = (FrameLayout) findViewById(R.id.apptentive_message_avatar_left);
		FrameLayout avatarFrameRight = (FrameLayout) findViewById(R.id.apptentive_message_avatar_right);

		AvatarView avatarView = new AvatarView(context);

		// Set up appearance based on incoming / outgoing.
		if(message.isOutgoingMessage()) {
			background.setBackgroundResource(R.drawable.apptentive_message_outgoing_2);
			avatarView.setAvatar(R.drawable.sky_bernal);
			avatarFrameRight.addView(avatarView);
			avatarFrameRight.setVisibility(View.VISIBLE);
			sender.setText("Sky Kelsey");
		} else {
			background.setBackgroundResource(R.drawable.apptentive_message_incoming_2);
			avatarView.setAvatar(R.drawable.apptentive_a_200x213);
			avatarFrameLeft.addView(avatarView);
			avatarFrameLeft.setVisibility(View.VISIBLE);
			sender.setText("Apptentive");
		}

		// Set timestamp
		// TODO: Move this to the middle, and only print a timestamp if later than 15 minutes since last message received.
		long time = Math.round(message.getCreatedAt() * 1000);
		timestamp.setText(Util.dateToDisplayString(time));

		// Set content
		text.setText(message.getBody());
	}
}
