/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterStatusView;

/**
 * @author Sky Kelsey
 */
public class StatusHolder extends MessageCenterListItemHolder {
	public TextView body;

	public StatusHolder(MessageCenterStatusView view) {
		body = (TextView) view.findViewById(R.id.body);
	}

	public void updateMessage(String body) {
		if (this.body != null) {
			this.body.setText(body);
		}
	}
}
