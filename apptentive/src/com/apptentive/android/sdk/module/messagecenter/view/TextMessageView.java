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
import android.widget.TextView;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.TextMessage;

/**
 * @author Sky Kelsey
 */
public class TextMessageView extends PersonalMessageView<TextMessage> {

	private CollapsibleTextView container;

	public TextMessageView(Context context, TextMessage message) {
		super(context, message);
	}

	protected void init(Context context, TextMessage message) {
		super.init(context, message);
		LayoutInflater inflater = LayoutInflater.from(context);
		FrameLayout bodyLayout = (FrameLayout) findViewById(R.id.body);
		View view = inflater.inflate(R.layout.apptentive_message_body_text,  bodyLayout);
		container = (CollapsibleTextView) view.findViewById(R.id.more_less_container);
	}

	public CollapsibleTextView getCollapsibleContainer() {
		return container;
	}
}
