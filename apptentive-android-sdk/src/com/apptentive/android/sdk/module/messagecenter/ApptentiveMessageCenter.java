/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.ViewActivity;
import com.apptentive.android.sdk.model.FileMessage;
import com.apptentive.android.sdk.model.Message;
import com.apptentive.android.sdk.model.TextMessage;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterView;

import java.util.List;


/**
 * @author Sky Kelsey
 */
public class ApptentiveMessageCenter {

	protected static MessageCenterView messageCenterView;

	public static void show(Context context) {
		Intent intent = new Intent();
		intent.setClass(context, ViewActivity.class);
		intent.putExtra("module", ViewActivity.Module.MESSAGE_CENTER.toString());
		context.startActivity(intent);
	}

	public static void doShow(final Context context) {
		if (!(context instanceof Activity)) {
			Log.e(ApptentiveMessageCenter.class.getSimpleName() + " must be initialized with an Activity Context.");
			return;
		}

		messageCenterView = new MessageCenterView((Activity) context, new MessageCenterView.OnSendMessageListener() {
			public void onSendTextMessage(String text) {
				final TextMessage message = new TextMessage();
				message.setBody(text);
				MessageManager.sendMessage(message);
				messageCenterView.post(new Runnable() {
					public void run() {
						messageCenterView.addMessage(message);
					}
				});
				scrollToBottom();
			}

			public void onSendFileMessage(Uri uri) {

				// First, create the file, and populate some metadata about it.
				final FileMessage message = new FileMessage();
				boolean successful = message.createStoredFile(uri.toString());

				if(successful) {
					// Finally, send out the message.
					MessageManager.sendMessage(message);
					messageCenterView.post(new Runnable() {
						public void run() {
							messageCenterView.addMessage(message);
						}
					});
					scrollToBottom();
				} else {
					Log.e("Unable to send file.");
					Toast.makeText(messageCenterView.getContext(), "Unable to send file.", Toast.LENGTH_SHORT);
				}
			}
		});
		scrollToBottom();

		// Remove an existing MessageCenterView and replace it with this, if it exists.
		if (messageCenterView.getParent() != null) {
			((ViewGroup) messageCenterView.getParent()).removeView(messageCenterView);
		}
		((Activity) context).setContentView(messageCenterView);

		// Display the messages we already have for starters.
		messageCenterView.setMessages(MessageManager.getMessages());

		// This listener will run when messages are retrieved from the server, and will start a new thread to update the view.
		final MessageManager.MessagesUpdatedListener listener = new MessageManager.MessagesUpdatedListener() {
			public boolean onMessagesUpdated() {
				messageCenterView.post(new Runnable() {
					public void run() {
						List<Message> messages = MessageManager.getMessages();
						messageCenterView.setMessages(messages);
					}
				});
				return false;
			}
		};

		MessageManager.asyncFetchAndStoreMessages(listener);

		// Give the MessageCenterView a callback when a message is sent.
		MessageManager.setInternalSentMessageListener(messageCenterView);
	}

	private static void scrollToBottom() {
		messageCenterView.post(new Runnable() {
			public void run() {
				ScrollView scroll = (ScrollView) messageCenterView.findViewById(R.id.apptentive_message_center_scrollview);
				scroll.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}
}


