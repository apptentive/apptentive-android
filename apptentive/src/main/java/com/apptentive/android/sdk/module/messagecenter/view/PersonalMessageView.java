/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.view.LayoutInflater;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;

/**
 * @author Sky Kelsey
 */
abstract public class PersonalMessageView<T extends ApptentiveMessage> extends MessageView<T> {

	public PersonalMessageView(Context context, final T message) {
		super(context, message);
	}

	/**
	 * Perform any view initialization here. Make sure to call super.init() first to initialise the parent hierarchy.
	 */
	protected void init(Context context, T message) {
		super.init(context, message);
		LayoutInflater inflater = LayoutInflater.from(context);
		if (message.isOutgoingMessage()) {
			inflater.inflate(R.layout.apptentive_message_outgoing, this);
		} else {
			inflater.inflate(R.layout.apptentive_message_incoming, this);
		}
	}
}
