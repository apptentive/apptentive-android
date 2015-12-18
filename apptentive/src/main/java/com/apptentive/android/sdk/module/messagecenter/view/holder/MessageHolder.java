/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.view.View;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.view.MessageView;

/**
 * @author Sky Kelsey
 */
public class MessageHolder extends MessageCenterListItemHolder {
	public TextView datestamp;
	public TextView status;


	public MessageHolder(MessageView view) {
		datestamp = (TextView) view.findViewById(R.id.datestamp);
		status = (TextView) view.findViewById(R.id.status);
	}

	public void updateMessage(String datestamp, int statusColor, String status) {
		if (this.datestamp != null) {
			this.datestamp.setText(datestamp);
			this.datestamp.setVisibility(datestamp != null ? View.VISIBLE : View.GONE);
		}
		if (this.status != null) {
			this.status.setText(status);
			this.status.setTextColor(statusColor);
			this.status.setVisibility(status != null ? View.VISIBLE : View.GONE);
		}
	}
}
