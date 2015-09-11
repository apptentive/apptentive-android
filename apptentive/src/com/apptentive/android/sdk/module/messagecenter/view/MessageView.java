/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.widget.FrameLayout;

import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;

/**
 * @author Sky Kelsey
 */
public abstract class MessageView<T extends ApptentiveMessage> extends FrameLayout implements MessageCenterListItemView {

	public MessageView(final Context context, final T message) {
		super(context);
		init(context, message);
	}

	protected void init(Context context, T message) {
	}
}
