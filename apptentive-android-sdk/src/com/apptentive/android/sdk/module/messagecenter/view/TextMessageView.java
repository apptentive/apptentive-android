/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.TextMessage;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey
 */
public class TextMessageView extends MessageView<TextMessage> {

	public TextMessageView(Context context, TextMessage message) {
		super(context, message);

		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.apptentive_message_center_text_message, this);

		LinearLayout messageRow = (LinearLayout) findViewById(R.id.apptentive_message_center_message_row);
		TextView text = (TextView) findViewById(R.id.apptentive_message_center_text_message_text);
		TextView timestamp = (TextView) findViewById(R.id.apptentive_message_center_timestamp);

		// Set content
		text.setText(message.getBody());

		// Set timestamp
		// TODO: Move this to the middle, and only print a timestamp if later than 15 minutes since last message received.
		timestamp.setText(Util.dateToDisplayString(Math.round(message.getCreatedAt() * 1000)));

		// Set up appearance based on incoming / outgoing.
		if(message.isOutgoingMessage()) {
			findViewById(R.id.apptentive_message_center_text_message_spacer_left).setVisibility(View.VISIBLE);
			messageRow.setGravity(Gravity.RIGHT);
			text.setBackgroundResource(R.drawable.apptentive_message_outgoing);
		} else {
			findViewById(R.id.apptentive_message_center_text_message_spacer_right).setVisibility(View.VISIBLE);
			messageRow.setGravity(Gravity.LEFT);
			text.setBackgroundResource(R.drawable.apptentive_message_incoming);
		}
	}
}
