/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;

import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;


import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.util.Util;


/**
 * @author Barry Li
 */
public class MessageCenterComposingActionBarView extends FrameLayout implements MessageCenterListItemView {


	public MessageCenterComposingActionBarView(Context context, final MessageAdapter.OnComposingActionListener listener) {
		super(context);

		LayoutInflater inflater = LayoutInflater.from(context);
		View parentView = inflater.inflate(R.layout.apptentive_message_center_composing_actionbar, this);

		View attachButton = findViewById(R.id.attach);
		// Android devices can't take screenshots until Android OS version 4+
		boolean canTakeScreenshot = Util.getMajorOsVersion() >= 4;
		if (canTakeScreenshot) {
			attachButton.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					listener.onAttachImage();
				}
			});
		} else {
			attachButton.setVisibility(GONE);
		}

		View sendButton = findViewById(R.id.send);
		sendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				listener.onFinishComposing();
			}
		});
	}

}