/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.Event;

import com.apptentive.android.sdk.module.engagement.interaction.model.MessageCenterInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.view.InteractionView;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.MessagePollingWorker;
import com.apptentive.android.sdk.module.messagecenter.model.IncomingTextMessage;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Barry Li
 */

public class MessageCenterActivityContent extends InteractionView<MessageCenterInteraction> {

	private final static String LIST_INSTANCE_STATE = "list";
	private final static String COMPOSING_EDITTEXT_STATE = "edittext";
	private final static String WHO_CARD_NAME = "whocardname";
	private final static String WHO_CARD_EMAIL = "whocardemail";
	private final static String WHO_CARD_AVATAR_FILE = "whocardavatar";

	private MessageCenterView messageCenterView;
	private Map<String, String> customData;
	private Context context;
	private MessageManager.OnNewIncomingMessagesListener newIncomingMessageListener;

	public MessageCenterActivityContent(MessageCenterInteraction interaction) {
		super(interaction);
	}

	public MessageCenterActivityContent(MessageCenterInteraction interaction, Serializable data) {
		this(interaction);
		this.customData = (Map<String, String>) data;
	}

	@Override
	public void doOnCreate(Activity activity, Bundle onSavedInstanceState) {

		context = activity;

		boolean bRestoreListView = onSavedInstanceState != null &&
				onSavedInstanceState.getParcelable(LIST_INSTANCE_STATE) != null;
		Parcelable editTextParcelable = (onSavedInstanceState == null) ? null :
				onSavedInstanceState.getParcelable(COMPOSING_EDITTEXT_STATE);
		String whoCardName = (onSavedInstanceState == null) ? null :
				onSavedInstanceState.getString(WHO_CARD_NAME);
		String whoCardEmail = (onSavedInstanceState == null) ? null :
				onSavedInstanceState.getString(WHO_CARD_EMAIL);
		String whoCardAvatarFile = (onSavedInstanceState == null) ? null :
				onSavedInstanceState.getString(WHO_CARD_AVATAR_FILE);
		String contextualMessage = interaction.getContextualMessageBody();

		messageCenterView = new MessageCenterView(activity, customData, contextualMessage,
				editTextParcelable, whoCardName, whoCardEmail, whoCardAvatarFile);

		// Remove an existing MessageCenterView and replace it with this, if it exists.
		if (messageCenterView.getParent() != null) {
			((ViewGroup) messageCenterView.getParent()).removeView(messageCenterView);
		}
		activity.setContentView(messageCenterView);

		newIncomingMessageListener = new MessageManager.OnNewIncomingMessagesListener() {
			public void onMessagesUpdated(final IncomingTextMessage apptentiveMsg) {
				messageCenterView.post(new Runnable() {
					public void run() {
						messageCenterView.addNewIncomingMessageItem(apptentiveMsg);
					}
				});
			}
		};

		// This listener will run when messages are retrieved from the server, and will start a new thread to update the view.
		MessageManager.addInternalOnMessagesUpdatedListener(newIncomingMessageListener);

		// Give the MessageCenterView a callback when a message is sent.
		MessageManager.setAfterSendMessageListener(messageCenterView);

		// Needed to prevent the window from being pushed up when a text input area is focused.
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED |
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		if (!bRestoreListView) {
			messageCenterView.scrollMessageListViewToBottomDelayed();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(LIST_INSTANCE_STATE, messageCenterView.onSaveListViewInstanceState());
		outState.putParcelable(COMPOSING_EDITTEXT_STATE, messageCenterView.onSaveEditTextInstanceState());
		outState.putString(WHO_CARD_NAME, messageCenterView.onSaveWhoCardName());
		outState.putString(WHO_CARD_EMAIL, messageCenterView.onSaveWhoCardEmail());
		outState.putString(WHO_CARD_AVATAR_FILE, messageCenterView.onSaveWhoCardAvatar());

		if (!messageCenterView.onSaveContextualMessage()) {
			interaction.clearContextualMessage();
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		messageCenterView.onRestoreListViewInstanceState(savedInstanceState.getParcelable(LIST_INSTANCE_STATE));
	}

	@Override
	public boolean onBackPressed(Activity activity) {
		messageCenterView.savePendingComposingMessage();
		clearPendingMessageCenterPushNotification();
		messageCenterView.clearComposingUi();
		messageCenterView.clearWhoCardUi();
		MetricModule.sendMetric(activity, Event.EventLabel.message_center__close);
		// Set to null, otherwise they will hold reference to the activity context
		MessageManager.clearInternalOnMessagesUpdatedListeners();
		MessageManager.setAfterSendMessageListener(null);
		return true;
	}

	public void onStart() {
		MessagePollingWorker.setMessageCenterInForeground(true);
	}

	public void onStop() {
		clearPendingMessageCenterPushNotification();
		MessagePollingWorker.setMessageCenterInForeground(false);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case Constants.REQUEST_CODE_PHOTO_FROM_MESSAGE_CENTER:
					messageCenterView.showAttachmentDialog(context, data.getData());
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void onPause() {
		MessageManager.onPauseSending();
	}

	@Override
	public void onResume() {
		MessageManager.onResumeSending();
	}

	private void clearPendingMessageCenterPushNotification() {
		SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
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
				MetricModule.sendError(context.getApplicationContext(), e, "Parsing Push notification", pushData);
			}
		}
	}
}
