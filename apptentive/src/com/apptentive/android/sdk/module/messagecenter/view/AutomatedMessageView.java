/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.view.LayoutInflater;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.CompoundMessage;

/**
 * @author Sky Kelsey
 */
public class AutomatedMessageView extends MessageView<CompoundMessage> {

	public AutomatedMessageView(Context context, CompoundMessage message) {
		super(context, message);
	}

	protected void init(Context context, CompoundMessage message) {
		super.init(context, message);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.apptentive_message_auto, this);
	}
}
