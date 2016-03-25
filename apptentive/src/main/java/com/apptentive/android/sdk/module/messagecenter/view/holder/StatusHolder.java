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
		body = (TextView) view.findViewById(R.id.status_body);
		icon = (ImageView) view.findViewById(R.id.icon);
	}

	public void updateMessage(final String bodyText, Integer iconRes) {
		if (body != null && body != null) {
			body.post(new Runnable() {
				@Override
				public void run() {
					body.setVisibility(TextView.VISIBLE);
					body.setText(bodyText);
				}
			});

		} else {
			body.post(new Runnable() {
				@Override
				public void run() {
					body.setVisibility(View.GONE);
				}
			});
		}
		if (icon != null && iconRes != null) {
			icon.setImageResource(iconRes);
			icon.setVisibility(View.VISIBLE);
		}
		else {
			icon.setVisibility(View.GONE);
		}
	}
}
