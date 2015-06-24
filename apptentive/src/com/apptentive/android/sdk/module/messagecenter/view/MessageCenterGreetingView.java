/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;

/**
 * @author Sky Kelsey
 */
public class MessageCenterGreetingView extends FrameLayout {

	MessageCenterGreeting messageCenterGreeting;

	public MessageCenterGreetingView(Context context, MessageCenterGreeting messageCenterGreeting) {
		super(context);

		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.apptentive_message_center_greeting, this);
	}

}
