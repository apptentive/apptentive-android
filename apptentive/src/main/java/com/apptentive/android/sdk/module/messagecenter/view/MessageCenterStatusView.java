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
import android.widget.ImageView;
import android.widget.TextView;

import com.apptentive.android.sdk.R;

/**
 * @author Barry Li
 */
public class MessageCenterStatusView extends FrameLayout implements MessageCenterListItemView {

	public TextView bodyTextView;
	public ImageView iconImageView;

	public MessageCenterStatusView(Context context) {
		super(context);

		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.apptentive_message_center_status, this);
		bodyTextView = (TextView) view.findViewById(R.id.body);
		iconImageView = (ImageView) view.findViewById(R.id.icon);
	}

}
