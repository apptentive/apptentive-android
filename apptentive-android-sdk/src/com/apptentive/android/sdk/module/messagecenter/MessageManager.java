/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;

import android.content.Context;
import android.content.SharedPreferences;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.model.AutomatedMessage;
import com.apptentive.android.sdk.model.Message;
import com.apptentive.android.sdk.model.MessageFactory;
import com.apptentive.android.sdk.storage.MessageStore;
import com.apptentive.android.sdk.storage.PayloadSendWorker;
import com.apptentive.android.sdk.util.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class MessageManager {

	private static OnSentMessageListener internalSentMessageListener;

	public static void asyncFetchAndStoreMessages(final MessagesUpdatedListener listener) {
		new Thread() {
			@Override
			public void run() {
				MessageManager.fetchAndStoreMessages(listener);
			}
		}.start();
	}

	/**
	 * Make sure to run this off the UI Thread, as it blocks on IO.
	 *
	 * @param listener
	 */
	public static void fetchAndStoreMessages(MessagesUpdatedListener listener) {
		if (GlobalInfo.conversationToken == null) {
			return;
		}
		// Fetch the messages.
		List<Message> messagesToSave = fetchMessages(getMessageStore().getLastReceivedMessageId());

		if (messagesToSave != null && messagesToSave.size() > 0) {
			Log.d("Messages retrieved.");
			// Mark messages from server where sender is the app user as read.
			for (Message message : messagesToSave) {
				if(message.isOutgoingMessage()) {
					message.setRead(true);
				}
			}
			getMessageStore().addOrUpdateMessages(messagesToSave.toArray(new Message[]{}));
			// Signal listener
			listener.onMessagesUpdated();
		}
	}

	public static List<Message> getMessages() {
		return getMessageStore().getAllMessages();
	}

	public static void sendMessage(Message message) {
		getMessageStore().addOrUpdateMessages(message);
		Apptentive.getDatabase().addPayload(message);
		PayloadSendWorker.start();
	}

	/**
	 * This doesn't need to be run during normal program execution. Testing only.
	 */
	public static void deleteAllRecords() {
		Log.e("Deleting all messages.");
		getMessageStore().deleteAllPayloads();
	}

	private static List<Message> fetchMessages(String after_id) {
		Log.d("Fetching messages newer than: " + after_id);
		ApptentiveHttpResponse response = ApptentiveClient.getMessages(null, after_id, null);

		List<Message> ret = new ArrayList<Message>();
		if (!response.isSuccessful()) {
			return ret;
		}
		try {
			ret = parseMessagesString(response.getContent());
		} catch (JSONException e) {
			Log.e("Error parsing messages JSON.", e);
		} catch (Exception e) {
			Log.e("Unexpected error parsing messages JSON.", e);
		}
		return ret;
	}

	public static void updateMessage(Message message) {
		getMessageStore().updateMessage(message);
	}

	protected static List<Message> parseMessagesString(String messageString) throws JSONException {
		List<Message> ret = new ArrayList<Message>();
			JSONObject root = new JSONObject(messageString);
			if (!root.isNull("items")) {
				JSONArray items = root.getJSONArray("items");
				for (int i = 0; i < items.length(); i++) {
					String json = items.getJSONObject(i).toString();
					Message message = MessageFactory.fromJson(json);
					// Since these came back from the server, mark them saved before updating them in the DB.
					message.setState(Message.State.saved);
					ret.add(message);
				}
			}
		return ret;
	}

	private static MessageStore getMessageStore() {
		return Apptentive.getDatabase();
	}

	public interface MessagesUpdatedListener {
		public void onMessagesUpdated();
	}

	public static void onSentMessage(Message message, ApptentiveHttpResponse response) {
		if (response == null || !response.isSuccessful()) {
			return;
		}
		if(response.isSuccessful()) {
			try {
				JSONObject responseJson = new JSONObject(response.getContent());
				if (message.getState() == Message.State.sending) {
					message.setState(Message.State.sent);
				}
				message.setId(responseJson.getString(Message.KEY_ID));
				message.setCreatedAt(responseJson.getDouble(Message.KEY_CREATED_AT));
			} catch (JSONException e) {
				Log.e("Error parsing sent message response.", e);
			}
			getMessageStore().updateMessage(message);

			if(internalSentMessageListener != null) {
				internalSentMessageListener.onSentMessage(message);
			}
		}
		if(response.isBadpayload()) {
			// TODO: Tell the user that this message failed to send. It will be deleted by the record send worker.
		}
	}

	public interface OnSentMessageListener {
		public void onSentMessage(Message message);
	}

	public static void setInternalSentMessageListener(OnSentMessageListener onSentMessageListener) {
		internalSentMessageListener = onSentMessageListener;
	}

	public static int getUnreadMessageCount() {
		return getMessageStore().getUnreadMessageCount();
	}

	public static void createMessageCenterAutoMessage(boolean forced) {
		SharedPreferences prefs = Apptentive.getAppContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		AutomatedMessage message = null;
		if(forced) {
			boolean shownManual = prefs.getBoolean(Constants.PREF_KEY_AUTO_MESSAGE_SHOWN_MANUAL, false);
			if(!shownManual) {
				prefs.edit().putBoolean(Constants.PREF_KEY_AUTO_MESSAGE_SHOWN_MANUAL, true).commit();
				message = AutomatedMessage.createWelcomeMessage();
			}
		} else {
			boolean shownManual = prefs.getBoolean(Constants.PREF_KEY_AUTO_MESSAGE_SHOWN_NO_LOVE, false);
			if(!shownManual) {
				prefs.edit().putBoolean(Constants.PREF_KEY_AUTO_MESSAGE_SHOWN_NO_LOVE, true).commit();
				message = AutomatedMessage.createNoLoveMessage();
			}
		}
		if(message != null) {
			getMessageStore().addOrUpdateMessages(message);
			Apptentive.getDatabase().addPayload(message);
		}
	}
}
