/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.view.CollapsibleTextView;
import com.apptentive.android.sdk.module.messagecenter.view.OutgoingTextMessageView;
import com.apptentive.android.sdk.view.ApptentiveMaterialIndeterminateProgressBar;

/**
 * @author Sky Kelsey
 */
public class OutgoingTextMessageHolder extends MessageHolder {
	public ApptentiveMaterialIndeterminateProgressBar progressBar;
	public CollapsibleTextView text;

	public OutgoingTextMessageHolder(OutgoingTextMessageView view) {
		super(view);
		progressBar = (ApptentiveMaterialIndeterminateProgressBar) view.findViewById(R.id.progressBar);
		text = (CollapsibleTextView) view.findViewById(R.id.more_less_container);
	}

	public void updateMessage(String timestamp, boolean progressBarVisible, String body) {
		super.updateMessage(timestamp);
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
			this.text.setDesc(body);
		}
	}
}
