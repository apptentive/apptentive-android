/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.Message;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey
 */
abstract public class PersonalMessageView<T extends Message> extends MessageView<T> {

	public PersonalMessageView(Context context, final T message) {
		super(context, message);
	}

	/**
	 * Perform any view initialization here. Make sure to call super.init() first to initialise the parent hierarchy.
	 */
	protected void init(T message) {
		super.init(message);
		LayoutInflater inflater = LayoutInflater.from(context);
		if (message.isOutgoingMessage()) {
			inflater.inflate(R.layout.apptentive_message_outgoing, this);
		} else {
			inflater.inflate(R.layout.apptentive_message_incoming, this);
		}
	}

	/**
	 * Call when you need to update the view with changed message contents. This should ONLY be called after the view has
	 * been initialized with a message.
	 *
	 * @param newMessage The new message whose contents we want to display.
	 */
	public void updateMessage(final T newMessage) {
		T oldMessage = message;
		super.updateMessage(newMessage);

		Double sentTime = message.getCreatedAt();

		// Set timestamp
		TextView timestampView = (TextView) findViewById(R.id.timestamp);
		timestampView.setText(createTimestamp(sentTime));

		// Set Progress indicator
		View progressBar = findViewById(R.id.progress);
		if (progressBar != null) {
			if (sentTime == null) {
				progressBar.setVisibility(View.VISIBLE);
			} else {
				progressBar.setVisibility(View.INVISIBLE);
			}
		}

		// Set avatar
		if (!message.isOutgoingMessage()) {
			AvatarView avatarView = (AvatarView) findViewById(R.id.avatar);
			String photoUrl = message.getSenderProfilePhoto();
			boolean avatarNeedsUpdate = oldMessage == null || (photoUrl != null && !photoUrl.equals(oldMessage.getSenderProfilePhoto()));
			if (avatarNeedsUpdate) {
				avatarView.fetchImage(message.getSenderProfilePhoto());
			}
		}
	}

	protected String createTimestamp(Double seconds) {
		Resources resources = context.getResources();
		if (seconds != null) {
			return Util.secondsToDisplayString(resources.getString(R.string.apptentive_message_sent_timestamp_format), seconds);
		}
		return resources.getString(R.string.apptentive_sending);
	}
}
