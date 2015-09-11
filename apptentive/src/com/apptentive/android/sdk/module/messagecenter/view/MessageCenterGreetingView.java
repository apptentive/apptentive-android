/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apptentive.android.sdk.AboutModule;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.ViewActivity;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;

/**
 * @author Sky Kelsey
 */
public class MessageCenterGreetingView extends FrameLayout implements MessageCenterListItemView {

	public ApptentiveAvatarView avatar;

	// Prevent info button being clicked multiple time, resulting multiple About
	protected static final int DELAY_TIME = 100;

	protected Handler mClickHandler = new Handler() {

		public void handleMessage(Message msg) {

			findViewById(msg.what).setClickable(true);
			super.handleMessage(msg);
		}
	};

	public MessageCenterGreetingView(final Context context, MessageCenterGreeting messageCenterGreeting) {
		super(context);

		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.apptentive_message_center_greeting, this);
		TextView title = (TextView) view.findViewById(R.id.title);
		TextView body = (TextView) view.findViewById(R.id.body);
		if (title != null) {
			title.setText(messageCenterGreeting.title);
		}
		if (body != null) {
			body.setText(messageCenterGreeting.body);
		}

		avatar = (ApptentiveAvatarView) view.findViewById(R.id.avatar);

		ImageButton infoButton = (ImageButton) findViewById(R.id.btn_info);
		infoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				view.setClickable(false);
				mClickHandler.sendEmptyMessageDelayed(view.getId(), DELAY_TIME);
				if (context instanceof ViewActivity)
				AboutModule.getInstance().show((Activity) context, false);
			}
		});
	}
}
