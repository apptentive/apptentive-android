/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
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

/**
 * @author Sky Kelsey
 */
public class TextMessageView3 extends MessageView<TextMessage> {

	public TextMessageView3(Context context, TextMessage message) {
		super(context, message);
	}

	public void setMessage(TextMessage message) {
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.apptentive_message_center_text_message_3, this);

		LinearLayout background = (LinearLayout) findViewById(R.id.apptentive_message_center_message_background);
		TextView nameView = (TextView) findViewById(R.id.apptentive_message_sender);
		TextView timestamp = (TextView) findViewById(R.id.apptentive_message_timestamp);
		TextView text = (TextView) findViewById(R.id.apptentive_message_center_text_message_text);
		FrameLayout avatarFrame;

		AvatarView avatarView = new AvatarView(context, message.getSenderProfilePhoto());

		// Set up appearance based on incoming / outgoing.
		if(message.isOutgoingMessage()) {
			avatarFrame = (FrameLayout) findViewById(R.id.apptentive_message_avatar_right);
			background.setBackgroundResource(R.drawable.apptentive_message_outgoing_3);
		} else {
			avatarFrame = (FrameLayout) findViewById(R.id.apptentive_message_avatar_left);
			background.setBackgroundResource(R.drawable.apptentive_message_incoming_3);
		}

		String name = message.getSenderUsername();
		if(name == null || name.equals("")) {
			name = message.isOutgoingMessage() ? "You" : "Them";
		}
		nameView.setText(name);

		avatarFrame.addView(new AvatarView(context, message.getSenderProfilePhoto()));
		avatarFrame.setVisibility(View.VISIBLE);

		// Set timestamp
		timestamp.setText(createTimestamp(message.getCreatedAt()));


		// Set content
		text.setText(message.getBody());
	}
}
