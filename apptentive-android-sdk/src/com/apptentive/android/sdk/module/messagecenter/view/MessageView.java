/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.apptentive.android.sdk.R;
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
		init(message);
		updateMessage(message);
	}

	/**
	 * Perform any view initialization here. Make sure to call super.init() first to initialise the parent hierarchy.
	 *
	 * @param message
	 */
	protected void init(T message) {
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
	public void updateMessage(T newMessage) {
		T oldMessage = message;
		message = newMessage;

		// Set timestamp
		TextView timestampView = (TextView) findViewById(R.id.apptentive_message_timestamp);
		timestampView.setText(createTimestamp(message.getCreatedAt()));

		// Set name
		TextView nameView = (TextView) findViewById(R.id.apptentive_message_sender_name);
		String name = message.getSenderUsername();
		if (name == null || name.equals("")) {
			Resources resources = context.getResources();
			name = newMessage.isOutgoingMessage() ? resources.getString(R.string.apptentive_you) : resources.getString(R.string.apptentive_them);
		}
		nameView.setText(name);

		// Set profile photo
		final FrameLayout avatarFrame = (FrameLayout) findViewById(R.id.apptentive_message_avatar);
		String photoUrl = message.getSenderProfilePhoto();
		boolean avatarNeedsUpdate = oldMessage == null || (photoUrl != null && !photoUrl.equals(oldMessage.getSenderProfilePhoto()));
		if (avatarNeedsUpdate) {
			// Perform the fetch on a new thread, and the UI update on the UI thread so we don't block everything.
			new Thread() {
				public void run() {
					final AvatarView avatar = new AvatarView(context, message.getSenderProfilePhoto());
					post(new Runnable() {
						public void run() {
							avatarFrame.addView(avatar);
						}
					});
				}
			}.start();
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
