/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.module.messagecenter.model.*;
import com.apptentive.android.sdk.offline.PayloadManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class MessageManager {

	/**
	 * Make sure to run this off the UI Thread.
	 *
	 * @param listener
	 */
	public static void fetchAndStoreMessages(MessagesUpdatedListener listener) {
		if (GlobalInfo.personId == null) {
			return;
		}
		// Fetch the messages.
		List<Message> messagesToSave = fetchMessages(getMessageStore().getLastMessageId());

		if (messagesToSave == null) {
			return;
		}
		// Store messages. Don't save messages we've already seen.
		List<Message> storedMessages = getMessageStore().getAllMessages();
		for (Message storedMessage : storedMessages) {
			for (Message messageToSave : messagesToSave) {
				String storedId = storedMessage.getMessageId();
				String currentId = messageToSave.getMessageId();
				if (storedId != null && currentId != null && storedId.equals(currentId)) {
					messagesToSave.remove(messageToSave);
					break;
				}
			}
		}
		getMessageStore().addMessages(messagesToSave.toArray(new Message[]{}));

		// Signal listener
		listener.onMessagesUpdated();
	}

	public static void sentMessage(String payloadId, ApptentiveHttpResponse response) {
		if (!response.wasSuccessful()) {
			return;
		}
		try {
			Message message = MessageManager.constructTypedMessage(response.getContent());
			getMessageStore().updateMessageWithPayloadId(payloadId, message);
		} catch (JSONException e) {
			Log.e("Error processing message response.", e);
		}

	}

	public static List<Message> getMessages() {
		return getMessageStore().getAllMessages();
	}

	public static void sendMessage(Message message) {
		PayloadManager.putPayload(message);
		getMessageStore().addMessages(message);
	}

	/**
	 * This doesn't need to be run during normal program execution.
	 */
	public static void deleteAllMessages() {
		Log.e("DELETING ALL MESSAGES!");
		getMessageStore().deleteAllMessages();
	}

	public static Message constructTypedMessage(String json) throws JSONException {
		return constructTypedMessage(json, null);
	}

	public static Message constructTypedMessage(String json, String typeString) throws JSONException {
		Message ret = null;
		Message.MessageType type = Message.MessageType.unknown;

		if (typeString == null) {
			Message message = new Message(json);
			type = message.getTypeEnum();
		} else {
			try {
				type = Message.MessageType.valueOf(typeString);
			} catch (IllegalArgumentException e) {
			}
		}

		switch (type) {
			case text_message:
				ret = new TextMessage(json);
				break;
			case upgrade_request:
				ret = new UpgradeRequest(json);
				break;
			case share_request:
				ret = new ShareRequest(json);
				break;
			case unknown:
				ret = new Message(json);
				break;
			default:
				break;
		}
		return ret;
	}

	private static List<Message> fetchMessages(String lastMessageGuid) {
		Log.d("Fetching messages newer than: " + lastMessageGuid);
		List<Message> ret = new ArrayList<Message>();
		ApptentiveHttpResponse response = ApptentiveClient.getMessages(GlobalInfo.personId, lastMessageGuid);

		if (!response.wasSuccessful()) {
			return ret;
		}

		try {
			JSONObject root = new JSONObject(response.getContent());
			if (root.has("messages")) {
				JSONArray messages = root.getJSONArray("messages");
				for (int i = 0; i < messages.length(); i++) {
					JSONObject json = messages.getJSONObject(i);
					Message message = MessageManager.constructTypedMessage(json.toString());
					ret.add(message);
				}
			}
		} catch (JSONException e) {
			Log.e("Error parsing messages JSON.", e);
		} catch (Exception e) {
			Log.e("Unexpected error parsing messages JSON.", e);
		}
		return ret;
	}

	private static MessageStore getMessageStore() {
		return Apptentive.getDatabase();
	}

	public interface MessagesUpdatedListener {
		public boolean onMessagesUpdated();
	}
}
