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
import android.widget.ImageButton;
import android.widget.TextView;


import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;
import com.apptentive.android.sdk.util.Util;


/**
 * @author Barry Li
 */
public class MessageCenterComposingActionBarView extends FrameLayout
		implements MessageCenterListItemView {


	public MessageCenterComposingActionBarView(Context context, final MessageCenterComposingItem item,
																						 final MessageAdapter.OnComposingActionListener listener) {
		super(context);

		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.apptentive_message_center_composing_actionbar, this);

		int pressedColor = Util.getThemeColorFromAttrOrRes(context, R.attr.apptentive_material_pressed_highlight,
				R.color.apptentive_material_pressed_highlight);

		View closeButton = findViewById(R.id.cancel_composing);
		closeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				listener.onCancelComposing();
			}
		});
		Util.setBackground(closeButton, Util.getSelectableImageButtonBackground(pressedColor));
		TextView composing = (TextView) findViewById(R.id.composing);

		if (item.str_1 != null) {
			composing.setText(item.str_1);
		}

		ImageButton sendButton = (ImageButton) findViewById(R.id.btn_send_message);
		if (item.button_1 != null) {
			sendButton.setContentDescription(item.button_1);
		}
		sendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				listener.onFinishComposing();
			}
		});
		Util.setBackground(sendButton, Util.getSelectableImageButtonBackground(pressedColor));
	}

}