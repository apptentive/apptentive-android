/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.TextMessage;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey
 */
public class TextMessageView extends MessageView {


	public TextMessageView(Context context, TextMessage message) {
		super(context, message);
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		int dips5 = Util.dipsToPixels(context, 5);
		setPadding(0, dips5, 0, dips5);

		LinearLayout row = new LinearLayout(context);
		row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		row.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout spacer = new LinearLayout(context);
		LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
		spacerParams.weight = 1;
		spacer.setLayoutParams(spacerParams);


		LinearLayout textRow = new LinearLayout(context);
		LinearLayout.LayoutParams textRowParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
		textRowParams.weight = 3;
		textRow.setLayoutParams(textRowParams);
		textRow.setGravity(message.isIncoming() ? Gravity.LEFT : Gravity.RIGHT);

		TextView textView = new TextView(context);
		LayoutParams textViewParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		textView.setLayoutParams(textViewParams);
		textView.setTextColor(Color.BLACK);
		textView.setBackgroundResource(message.isIncoming() ? R.drawable.apptentive_message_incoming : R.drawable.apptentive_message_outgoing);
		textView.setText(message.getText());

		textRow.addView(textView);
		if(message.isIncoming()) {
			row.addView(textRow);
			row.addView(spacer);
		} else {
			row.addView(spacer);
			row.addView(textRow);
		}
		addView(row);
	}
}
