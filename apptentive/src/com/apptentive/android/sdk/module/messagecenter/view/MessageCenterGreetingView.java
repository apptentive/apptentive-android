/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;

/**
 * @author Sky Kelsey
 */
public class MessageCenterGreetingView extends FrameLayout {

	Context context;
	MessageCenterGreeting messageCenterGreeting;

	public MessageCenterGreetingView(Context context, MessageCenterGreeting messageCenterGreeting) {
		super(context);
		this.context = context;

		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.apptentive_message_center_greeting, this);
		updateMessage(messageCenterGreeting);
	}

	public void updateMessage(MessageCenterGreeting messageCenterGreeting) {
		this.messageCenterGreeting = messageCenterGreeting;
		String titleText = messageCenterGreeting.getTitle();
		if (titleText != null) {
			TextView title = (TextView) findViewById(R.id.title);
			title.setText(titleText);
		}

		String bodyText = messageCenterGreeting.getBody();
		if (bodyText != null) {
			TextView body = (TextView) findViewById(R.id.body);
			body.setText(bodyText);
		}
	}
}
