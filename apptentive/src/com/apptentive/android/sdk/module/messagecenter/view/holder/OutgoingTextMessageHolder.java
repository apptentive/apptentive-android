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
	public TextView text;

	public OutgoingTextMessageHolder(OutgoingTextMessageView view) {
		super(view);
		progressBar = (ApptentiveMaterialIndeterminateProgressBar) view.findViewById(R.id.progressBar);
		text = (TextView) view.findViewById(R.id.text);
	}

	public void updateMessage(String datestamp, String status, boolean progressBarVisible, String body) {
		super.updateMessage(datestamp, status);
		if (this.progressBar != null) {
			if (progressBarVisible) {
				this.progressBar.start();
				this.progressBar.setVisibility(View.VISIBLE);
			} else {
				this.progressBar.stop();
				this.progressBar.setVisibility(View.INVISIBLE);
			}
		}
		if (this.text != null) {
			this.text.setText(body);
		}
	}
}
