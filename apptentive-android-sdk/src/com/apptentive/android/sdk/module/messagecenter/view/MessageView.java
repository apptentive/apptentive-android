/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.widget.FrameLayout;
import com.apptentive.android.sdk.module.messagecenter.model.Message;

/**
 * @author Sky Kelsey
 */
abstract public class MessageView<T extends Message> extends FrameLayout {

	protected Context context;
	protected T message;

	public MessageView(Context context, T message) {
		super(context);
		this.context = context;
		this.message = message;
	}
}
