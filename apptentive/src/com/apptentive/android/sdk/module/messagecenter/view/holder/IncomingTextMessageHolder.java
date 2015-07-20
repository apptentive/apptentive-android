/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.graphics.Bitmap;

import com.apptentive.android.sdk.R;

import com.apptentive.android.sdk.module.messagecenter.view.ApptentiveAvatarView;
import com.apptentive.android.sdk.module.messagecenter.view.CollapsibleTextView;
import com.apptentive.android.sdk.module.messagecenter.view.IncomingTextMessageView;

/**
 * @author Sky Kelsey
 */
public class IncomingTextMessageHolder extends MessageHolder {

	public ApptentiveAvatarView avatar;
	public CollapsibleTextView text;

	public IncomingTextMessageHolder(IncomingTextMessageView view) {
		super(view);
		avatar = (ApptentiveAvatarView) view.findViewById(R.id.avatar);
		text = (CollapsibleTextView) view.findViewById(R.id.more_less_container);
	}

	public void updateMessage(String timestamp, String text) {
		super.updateMessage(timestamp);

		if (this.text != null) {
			this.text.setDesc(text);
		}
	}
}
