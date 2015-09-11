/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.view.View;
import android.widget.ImageView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.view.FileMessageView;
import com.apptentive.android.sdk.view.ApptentiveMaterialIndeterminateProgressBar;

/**
 * @author Sky Kelsey
 */
public class OutgoingFileMessageHolder extends MessageHolder {
	public ApptentiveMaterialIndeterminateProgressBar progressBar;
	public ImageView image;

	public OutgoingFileMessageHolder(FileMessageView view) {
		super(view);
		progressBar = (ApptentiveMaterialIndeterminateProgressBar) view.findViewById(R.id.progressBar);
		image = (ImageView) view.findViewById(R.id.apptentive_file_message_image);
	}

	public void updateMessage(String timestamp, String status, int statusColor, boolean progressBarVisible) {
		super.updateMessage(timestamp, statusColor, status);
		if (this.progressBar != null) {
			if (progressBarVisible) {
				this.progressBar.start();
				this.progressBar.setVisibility(View.VISIBLE);
			} else {
				this.progressBar.stop();
				this.progressBar.setVisibility(View.GONE);
			}
		}
	}
}
