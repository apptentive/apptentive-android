/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.view.View;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.view.CollapsibleTextView;
import com.apptentive.android.sdk.module.messagecenter.view.OutgoingTextMessageView;
import com.apptentive.android.sdk.view.ApptentiveMaterialIndeterminateProgressBar;

/**
 * @author Sky Kelsey
 */
public class OutgoingTextMessageHolder extends MessageHolder {
	public ApptentiveMaterialIndeterminateProgressBar progressBar;
	public CollapsibleTextView messageContentView;

	public OutgoingTextMessageHolder(OutgoingTextMessageView view) {
		super(view);
		progressBar = (ApptentiveMaterialIndeterminateProgressBar) view.findViewById(R.id.progressBar);
		messageContentView = (CollapsibleTextView) view.findViewById(R.id.more_less_container);
	}

	public void updateMessage(String datestamp, String status, boolean progressBarVisible, String body) {
		super.updateMessage(datestamp, status);
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
			messageContentView.setDesc(body);
		}
	}
}
