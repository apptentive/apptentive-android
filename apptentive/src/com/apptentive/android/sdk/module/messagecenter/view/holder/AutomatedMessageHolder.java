/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.view.View;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.AutomatedMessage;
import com.apptentive.android.sdk.module.messagecenter.view.AutomatedMessageView;

/**
 * @author Barry Li
 */
public class AutomatedMessageHolder extends MessageHolder {
	public TextView body;

	public AutomatedMessageHolder(AutomatedMessageView view) {
		super(view);
		body = (TextView) view.findViewById(R.id.apptentive_message_auto_body);
	}

	public void updateMessage(String dateStamp, final AutomatedMessage newMessage) {
		super.updateMessage(dateStamp, 0, null);
		body.setText(newMessage.getBody());
	}
}