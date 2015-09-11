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
import com.apptentive.android.sdk.module.messagecenter.model.OutgoingFileMessage;

/**
 * @author Sky Kelsey
 */
public class FileMessageView extends PersonalMessageView<OutgoingFileMessage> {


	public FileMessageView(Context context, OutgoingFileMessage message) {
		super(context, message);
	}

	protected void init(Context context, OutgoingFileMessage message) {
		super.init(context, message);
		LayoutInflater inflater = LayoutInflater.from(context);
		FrameLayout bodyLayout = (FrameLayout) findViewById(R.id.body);
		inflater.inflate(R.layout.apptentive_message_body_file, bodyLayout);
	}

}
