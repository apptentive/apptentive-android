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
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterStatus;


/**
 * @author Barry Li
 */
public class MessageCenterStatusView extends FrameLayout implements MessageCenterListItemView {


	public MessageCenterStatusView(Context context, MessageCenterStatus status) {
		super(context);

		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.apptentive_message_center_status, this);
	}

	/*public void updateMessage(MessageCenterStatus status) {

		String titleText = status.getTitle();
		if (titleText != null) {
			TextView title = (TextView) findViewById(R.id.title);
			title.setText(titleText);
		}

		if (status.getBody() != null) {
			TextView body = (TextView) findViewById(R.id.body);
			body.setText(status.getBody());
		}
	}*/
}
