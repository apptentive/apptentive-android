/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.widget.FrameLayout;
import com.apptentive.android.sdk.model.Message;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey
 */
abstract public class MessageView<T extends Message> extends FrameLayout {

	protected Context context;
	protected T message;

	public MessageView(Context context, T message) {
		super(context);
		this.context = context;
		setMessage(message);
	}

	abstract void setMessage(T message);

	protected String createTimestamp(Double seconds) {
		if(seconds!= null) {
			return Util.secondsToDisplayString(seconds);
		}
		return "Sending...";
	}
}
