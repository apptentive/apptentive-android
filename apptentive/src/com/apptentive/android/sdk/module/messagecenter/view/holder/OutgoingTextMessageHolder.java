/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.view.View;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.view.OutgoingTextMessageView;
import com.apptentive.android.sdk.view.ApptentiveMaterialIndeterminateProgressBar;

/**
 * @author Sky Kelsey
 */
public class OutgoingTextMessageHolder extends MessageHolder {
	public ApptentiveMaterialIndeterminateProgressBar progressBar;
	public TextView messageContentView;

	public OutgoingTextMessageHolder(OutgoingTextMessageView view) {
		super(view);
		progressBar = (ApptentiveMaterialIndeterminateProgressBar) view.findViewById(R.id.progressBar);
		messageContentView = (TextView) view.findViewById(R.id.more_less_container);
	}

	public void updateMessage(String datestamp, String status, int statusColor,
														boolean progressBarVisible, String body) {
		super.updateMessage(datestamp, statusColor, status);
		if (progressBar != null) {
			if (progressBarVisible) {
				progressBar.start();
				progressBar.setVisibility(View.VISIBLE);
			} else {
				progressBar.stop();
				progressBar.setVisibility(View.GONE);
			}
		}
		if (messageContentView != null) {
			messageContentView.setText(body);
		}
	}
}
