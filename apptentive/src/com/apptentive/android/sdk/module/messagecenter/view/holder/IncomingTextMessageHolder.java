/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.view.View;
import android.widget.TextView;

import com.apptentive.android.sdk.R;

import com.apptentive.android.sdk.module.messagecenter.view.ApptentiveAvatarView;
import com.apptentive.android.sdk.module.messagecenter.view.IncomingTextMessageView;

/**
 * @author Sky Kelsey
 */
public class IncomingTextMessageHolder extends MessageHolder {

	public ApptentiveAvatarView avatar;
	private TextView messageBody;
	private TextView nameView;

	public IncomingTextMessageHolder(IncomingTextMessageView view) {
		super(view);
		avatar = (ApptentiveAvatarView) view.findViewById(R.id.avatar);
		messageBody = (TextView) view.findViewById(R.id.more_less_container);
		nameView = (TextView) view.findViewById(R.id.sender_name);
	}

	public void updateMessage(String name, String datestamp, String text) {
		super.updateMessage(datestamp, 0, null);

		if (messageBody != null) {
			messageBody.setText(text);
		}

		if (name != null && !name.isEmpty()) {
			nameView.setVisibility(View.VISIBLE);
			nameView.setText(name);
		} else {
			nameView.setVisibility(View.GONE);
		}
	}
}
