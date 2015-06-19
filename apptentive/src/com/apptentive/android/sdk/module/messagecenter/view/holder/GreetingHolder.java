/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterGreetingView;

/**
 * @author Sky Kelsey
 */
public class GreetingHolder extends MessageCenterListItemHolder {
	public TextView title;
	public TextView body;

	public GreetingHolder(MessageCenterGreetingView view) {
		title = (TextView) view.findViewById(R.id.title);
		body = (TextView) view.findViewById(R.id.body);
	}

	public void updateMessage(String title, String body) {
		if (this.title != null) {
			this.title.setText(title);
		}
		if (this.body != null) {
			this.body.setText(body);
		}
	}
}
