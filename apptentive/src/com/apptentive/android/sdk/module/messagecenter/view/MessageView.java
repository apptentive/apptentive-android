/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.widget.FrameLayout;

import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.module.messagecenter.model.Message;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.metric.MetricModule;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sky Kelsey
 */
public abstract class MessageView<T extends Message> extends FrameLayout implements MessageCenterListItemView {

	public MessageView(final Context context, final T message) {
		super(context);
		init(context, message);
	}

	protected void init(Context context, T message) {
	}
}
