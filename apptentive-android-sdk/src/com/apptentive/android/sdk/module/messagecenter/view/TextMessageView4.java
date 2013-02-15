/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.TextMessage;

/**
 * TODO: Move most of this into the MessageView parent class.
 * @author Sky Kelsey
 */
public class TextMessageView4 extends MessageView<TextMessage> {

	public TextMessageView4(Context context, TextMessage message) {
		super(context, message);
	}

	public void setMessage(final TextMessage newMessage) {
		boolean init = message == null;
		message = newMessage;

		if (init) {
			LayoutInflater inflater = LayoutInflater.from(context);
			if (message.isOutgoingMessage()) {
				inflater.inflate(R.layout.apptentive_message_center_text_message_4_outgoing, this);
			} else {
				inflater.inflate(R.layout.apptentive_message_center_text_message_4_incoming, this);
			}
		}

		TextView nameView = (TextView) findViewById(R.id.apptentive_message_sender_name);
		TextView timestampView = (TextView) findViewById(R.id.apptentive_message_timestamp);
		TextView textView = (TextView) findViewById(R.id.apptentive_text_message_text);
		final FrameLayout avatarFrame = (FrameLayout) findViewById(R.id.apptentive_message_avatar);

		if (init) {
			// Set up appearance based on incoming / outgoing.
			if (message.isOutgoingMessage()) {
				textView.setBackgroundResource(R.drawable.apptentive_message_bubble_outgoing_top);
			} else {
				textView.setBackgroundResource(R.drawable.apptentive_message_bubble_incoming_top);
			}
		}

		// Set content
		textView.setText(newMessage.getBody());

		// Set timestamp
		timestampView.setText(createTimestamp(message.getCreatedAt()));

		// Set name
		String name = message.getSenderUsername();
		if(name == null || name.equals("")) {
			name = message.isOutgoingMessage() ? "You" : "Them";
		}
		nameView.setText(name);

		post(new Runnable() {
			public void run() {
				avatarFrame.addView(new AvatarView(context, message.getSenderProfilePhoto()));
			}
		});
//		invalidate();
	}
}
