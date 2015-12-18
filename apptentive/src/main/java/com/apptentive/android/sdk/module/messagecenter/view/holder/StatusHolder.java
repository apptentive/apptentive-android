/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterStatusView;

/**
 * @author Sky Kelsey
 */
public class StatusHolder extends MessageCenterListItemHolder {
	private TextView body;
	private ImageView icon;

	public StatusHolder(MessageCenterStatusView view) {
		body = (TextView) view.findViewById(R.id.body);
		icon = (ImageView) view.findViewById(R.id.icon);
	}

	public void updateMessage(String body, Integer icon) {
		if (this.body != null && body != null) {
			this.body.setVisibility(TextView.VISIBLE);
			this.body.setText(body);
		} else {
			this.body.setVisibility(View.GONE);
		}
		if (this.icon != null && icon != null) {
			this.icon.setImageResource(icon);
			this.icon.setVisibility(View.VISIBLE);
		}
		else {
			this.icon.setVisibility(View.GONE);
		}
	}
}
