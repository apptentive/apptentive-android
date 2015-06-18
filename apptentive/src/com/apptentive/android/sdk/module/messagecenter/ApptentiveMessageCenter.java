/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;
import android.view.ViewGroup;
import android.widget.Toast;

import com.apptentive.android.sdk.*;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterStatus;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterView;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;


/**
 * @author Sky Kelsey
 */
public class ApptentiveMessageCenter {

	protected static MessageCenterView messageCenterView;
	private static Map<String, String> customData;

	public static void show(Activity activity, Map<String, String> customData) {
		ApptentiveMessageCenter.customData = customData;

		Intent intent = new Intent();
		intent.setClass(activity, ViewActivity.class);
		intent.putExtra(ActivityContent.KEY, ActivityContent.Type.MESSAGE_CENTER.toString());
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_up_in, R.anim.slide_down_out);
	}


	/**
	 * @param activity The Activity Context that launched this view.
	 */
	public static void doShow(final Activity activity) {
		MetricModule.sendMetric(activity.getApplicationContext(), Event.EventLabel.message_center__launch);

		MessageCenterView.OnSendMessageListener onSendMessageListener = new MessageCenterView.OnSendMessageListener() {
			public void onSendTextMessage(String text) {
				final TextMessage message = new TextMessage();
				message.setBody(text);
				message.setRead(true);
				message.setCustomData(customData);
				customData = null;
				MessageManager.sendMessage(activity.getApplicationContext(), message);
				messageCenterView.post(new Runnable() {
					public void run() {
						messageCenterView.addItem(message);
						messageCenterView.onResume();
					}
				});
				scrollToBottom();
			}

			public void onSendFileMessage(Uri uri) {
				// First, create the file, and populate some metadata about it.
				final FileMessage message = new FileMessage();
				boolean successful = message.internalCreateStoredImage(activity.getApplicationContext(), uri.toString());
				if (successful) {
					message.setRead(true);
					message.setCustomData(customData);
					customData = null;
					// Finally, send out the message.
					MessageManager.sendMessage(activity.getApplicationContext(), message);
					messageCenterView.post(new Runnable() {
						public void run() {
							messageCenterView.addItem(message);
							messageCenterView.onResume();
						}
					});
					scrollToBottom();
				} else {
					Log.e("Unable to send file.");
					Toast.makeText(messageCenterView.getContext(), "Unable to send file.", Toast.LENGTH_SHORT).show();
				}
			}
		};

		messageCenterView = new MessageCenterView(activity, onSendMessageListener);

		// Remove an existing MessageCenterView and replace it with this, if it exists.
		if (messageCenterView.getParent() != null) {
			((ViewGroup) messageCenterView.getParent()).removeView(messageCenterView);
		}
		activity.setContentView(messageCenterView);

		// This listener will run when messages are retrieved from the server, and will start a new thread to update the view.
		MessageManager.setInternalOnMessagesUpdatedListener(new MessageManager.OnNewMessagesListener() {
			public void onMessagesUpdated() {
				messageCenterView.post(new Runnable() {
					public void run() {
						List<MessageCenterListItem> items = MessageManager.getMessageCenterListItems(activity.getApplicationContext());
						messageCenterView.setItems(items);
						scrollToBottom();
					}
				});
			}
		});

		// Change to foreground polling, which polls more often.
		MessagePollingWorker.setMessageCenterInForeground(true);

		// Give the MessageCenterView a callback when a message is sent.
		MessageManager.setSentMessageListener(messageCenterView);

		scrollToBottom();
	}

	public static void clearPendingMessageCenterPushNotification(Activity activity) {
		SharedPreferences prefs = activity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String pushData = prefs.getString(Constants.PREF_KEY_PENDING_PUSH_NOTIFICATION, null);
		if (pushData != null) {
			try {
				JSONObject pushJson = new JSONObject(pushData);
				ApptentiveInternal.PushAction action = ApptentiveInternal.PushAction.unknown;
				if (pushJson.has(ApptentiveInternal.PUSH_ACTION)) {
					action = ApptentiveInternal.PushAction.parse(pushJson.getString(ApptentiveInternal.PUSH_ACTION));
				}
				switch (action) {
					case pmc:
						Log.i("Clearing pending Message Center push notification.");
						prefs.edit().remove(Constants.PREF_KEY_PENDING_PUSH_NOTIFICATION).commit();
						break;
				}
			} catch (JSONException e) {
				Log.w("Error parsing JSON from push notification.", e);
				MetricModule.sendError(activity.getApplicationContext(), e, "Parsing Push notification", pushData);
			}
		}
	}

	public static void scrollToBottom() {
		messageCenterView.scrollMessageListViewToBottom();
	}

	public static void onStop(Activity activity) {
		clearPendingMessageCenterPushNotification(activity);
		// Remove listener here.
		MessagePollingWorker.setMessageCenterInForeground(false);
	}

	public static boolean onBackPressed(Activity activity) {
		clearPendingMessageCenterPushNotification(activity);
		MetricModule.sendMetric(activity, Event.EventLabel.message_center__close);
		return true;
	}

	enum Trigger {
		enjoyment_dialog,
		message_center
	}
}
