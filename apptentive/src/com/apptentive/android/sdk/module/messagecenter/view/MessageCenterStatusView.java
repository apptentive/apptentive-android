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

/**
 * @author Barry Li
 */
public class MessageCenterStatusView extends FrameLayout implements MessageCenterListItemView {

	public TextView title;
	public TextView body;

	public MessageCenterStatusView(Context context) {
		super(context);

		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.apptentive_message_center_status, this);
		title = (TextView) view.findViewById(R.id.title);
		body = (TextView) view.findViewById(R.id.body);
	}

	public void updateMessage(String title, String body) {
		if (this.title != null) {
			this.title.setVisibility(View.VISIBLE);
			this.title.setText(title);
		} else {
			this.title.setVisibility(View.GONE);
		}
		if (this.body != null) {
			this.body.setVisibility(View.VISIBLE);
			this.body.setText(body);
		} else {
			this.body.setVisibility(View.GONE);
		}
	}

}
