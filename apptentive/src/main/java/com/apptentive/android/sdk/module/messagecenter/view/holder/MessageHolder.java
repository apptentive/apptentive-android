/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.text.TextUtils;
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

	public void updateMessage(final String datestampString, final int statusColor, final String statusString) {
		if (datestamp != null) {
			datestamp.post(new Runnable() {
				@Override
				public void run() {
					datestamp.setText(datestampString);
					datestamp.setVisibility(!TextUtils.isEmpty(datestampString) ? View.VISIBLE : View.GONE);
				}
			});
		}
		if (status != null) {
			status.post(new Runnable() {
				@Override
				public void run() {
					status.setText(statusString);
					status.setTextColor(statusColor);
					status.setVisibility(!TextUtils.isEmpty(statusString) ? View.VISIBLE : View.GONE);
				}
			});
		}
	}
}
