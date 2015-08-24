/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.widget.TextView;

import com.apptentive.android.sdk.R;

import com.apptentive.android.sdk.module.messagecenter.view.ApptentiveAvatarView;
import com.apptentive.android.sdk.module.messagecenter.view.IncomingTextMessageView;

/**
 * @author Sky Kelsey
 */
public class IncomingTextMessageHolder extends MessageHolder {

	public ApptentiveAvatarView avatar;
	public TextView text;

	public IncomingTextMessageHolder(IncomingTextMessageView view) {
		super(view);
		avatar = (ApptentiveAvatarView) view.findViewById(R.id.avatar);
		text = (TextView) view.findViewById(R.id.more_less_container);
	}

	public void updateMessage(String datestamp, String text) {
		super.updateMessage(datestamp, 0, null);

		if (this.text != null) {
			this.text.setText(text);
		}
	}
}
