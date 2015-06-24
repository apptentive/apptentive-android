/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.view.MessageView;

/**
 * @author Sky Kelsey
 */
public class MessageHolder extends MessageCenterListItemHolder {
	public TextView timestamp;

	public MessageHolder(MessageView view) {
		timestamp = (TextView) view.findViewById(R.id.timestamp);
	}

	public void updateMessage(String timestamp) {
		this.timestamp.setText(timestamp);
	}
}
