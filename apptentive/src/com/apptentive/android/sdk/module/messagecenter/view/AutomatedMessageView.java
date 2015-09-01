/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.AutomatedMessage;

/**
 * @author Sky Kelsey
 */
public class AutomatedMessageView extends MessageView<AutomatedMessage> {

	public AutomatedMessageView(Context context, AutomatedMessage message) {
		super(context, message);
	}

	protected void init(Context context, AutomatedMessage message) {
		super.init(context, message);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.apptentive_message_auto, this);
	}
}
