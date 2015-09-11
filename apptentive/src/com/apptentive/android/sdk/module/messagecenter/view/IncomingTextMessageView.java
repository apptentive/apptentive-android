/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.IncomingTextMessage;

/**
 * @author Sky Kelsey
 */
public class IncomingTextMessageView extends PersonalMessageView<IncomingTextMessage> {


	public IncomingTextMessageView(Context context, IncomingTextMessage message) {
		super(context, message);
	}

	protected void init(Context context, IncomingTextMessage message) {
		super.init(context, message);
		LayoutInflater inflater = LayoutInflater.from(context);
		FrameLayout bodyLayout = (FrameLayout) findViewById(R.id.body);
		inflater.inflate(R.layout.apptentive_message_body_text,  bodyLayout);
	}


}
