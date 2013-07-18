/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.rating.view.ApptentiveBaseDialog;

/**
 * @author Sky Kelsey
 */
public class MessageCenterThankYouDialog extends ApptentiveBaseDialog {

	private OnChoiceMadeListener onChoiceMadeListener;

	public MessageCenterThankYouDialog(Context context) {
		super(context, R.layout.apptentive_message_center_thank_you_dialog);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		final Button close = (Button) findViewById(R.id.close);
		final Button viewMessages = (Button) findViewById(R.id.view_messages);

		close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				cancel();
				if(onChoiceMadeListener != null) {
					onChoiceMadeListener.onNo();
				}
			}
		});

		viewMessages.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
				if(onChoiceMadeListener != null) {
					onChoiceMadeListener.onYes();
				}
			}
		});
	}

	public void setOnChoiceMadeListener(OnChoiceMadeListener onChoiceMadeListener) {
		this.onChoiceMadeListener = onChoiceMadeListener;
	}

	public interface OnChoiceMadeListener {
		public void onNo();
		public void onYes();
	}
}
