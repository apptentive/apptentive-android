/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;


import com.apptentive.android.sdk.R;


/**
 * @author Barry Li
 */
public class MessageCenterComposingActionBarView extends FrameLayout implements MessageCenterListItemView {


	public MessageCenterComposingActionBarView(Context context, final MessageAdapter.OnComposingActionListener listener) {
		super(context);

		LayoutInflater inflater = LayoutInflater.from(context);
		View parentView = inflater.inflate(R.layout.apptentive_message_center_composing_actionbar, this);

		View closeButton = findViewById(R.id.cancel_composing);
		closeButton.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					listener.onCancelComposing();
				}
			});


		View sendButton = findViewById(R.id.BtnSend);
		sendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				listener.onFinishComposing();
			}
		});
	}

}