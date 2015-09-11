/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.ViewActivity;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.interaction.model.MessageCenterInteraction;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveToastNotification;
import com.apptentive.android.sdk.module.messagecenter.model.IncomingTextMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil;
import com.apptentive.android.sdk.module.messagecenter.model.OutgoingFileMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageFactory;
import com.apptentive.android.sdk.storage.ApptentiveDatabase;
import com.apptentive.android.sdk.storage.MessageStore;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class MessageManager {

	// The reason od message sending errors
	public static int SEND_PAUSE_REASON_NETWORK = 1;
	public static int SEND_PAUSE_REASON_SERVER = 2;

	private static int TOAST_TYPE_UNREAD_MESSAGE = 1;

	private static final int UI_THREAD_MESSAGE_ON_UNREAD_HOST = 1;
	private static final int UI_THREAD_MESSAGE_ON_UNREAD_INTERNAL = 2;
	private static final int UI_THREAD_MESSAGE_ON_TOAST_NOTIFICATION = 3;

	private static WeakReference<Activity> currentForgroundApptentiveActivity;

	private static WeakReference<AfterSendMessageListener> afterSendMessageListener;

	private static final List<WeakReference<OnNewIncomingMessagesListener>> internalNewMessagesListeners = new ArrayList<WeakReference<OnNewIncomingMessagesListener>>();


	/* UnreadMessagesListener is set by external hosting app, and its lifecycle is managed by the app.
	 * Use WeakReference to prevent memory leak
	 */
	private static final List<WeakReference<UnreadMessagesListener>> hostUnreadMessagesListeners = new ArrayList<WeakReference<UnreadMessagesListener>>();

	private static Handler sUIHandler;

	private static Handler getHandlerInstance() {
		if (sUIHandler == null) {
			sUIHandler = new Handler(Looper.getMainLooper()) {
				@Override
				public void handleMessage(android.os.Message msg) {
					switch (msg.what) {
						case UI_THREAD_MESSAGE_ON_UNREAD_HOST:
							notifyHostUnreadMessagesListeners(msg.arg1);
							break;
						case UI_THREAD_MESSAGE_ON_UNREAD_INTERNAL: {
							IncomingTextMessage msgToAdd = (IncomingTextMessage) msg.obj;
							notifyInternalNewMessagesListeners(msgToAdd);
							break;
						}
						case UI_THREAD_MESSAGE_ON_TOAST_NOTIFICATION: {
							IncomingTextMessage msgToShow = (IncomingTextMessage) msg.obj;
							showUnreadMessageToastNotification(msgToShow);
							break;
						}
						default:
							super.handleMessage(msg);
					}
				}
			};
		}
		return sUIHandler;
	}

	/**
	 * Performs a request against the server to check for messages in the conversation since the latest message we already have.
	 * Make sure to run this off the UI Thread, as it blocks on IO.
	 *
	 * @return true if messages were returned, else false.
	 */
	public static boolean fetchAndStoreMessages(Context appContext, boolean forMessageCenter, boolean showToast) {
		if (GlobalInfo.conversationToken == null) {
			return false;
		}
		if (!Util.isNetworkConnectionPresent(appContext)) {
			return false;
		}

		// Fetch the messages.
		String lastId = getMessageStore(appContext).getLastReceivedMessageId();
		Log.d("Fetching messages after last id: " + lastId);
		List<ApptentiveMessage> messagesToSave = fetchMessages(appContext, lastId);

		IncomingTextMessage messageOnToast = null;
		if (messagesToSave != null && messagesToSave.size() > 0) {
			Log.d("Messages retrieved.");
			// Also get the count of incoming unread messages.
			int incomingUnreadMessages = 0;
			// Mark messages from server where sender is the app user as read.
			for (ApptentiveMessage apptentiveMessage : messagesToSave) {
				if (apptentiveMessage.isOutgoingMessage()) {
					apptentiveMessage.setRead(true);
				} else {
					if (messageOnToast == null) {
						if (apptentiveMessage.getType() == ApptentiveMessage.Type.TextMessage) {
							messageOnToast = (IncomingTextMessage) apptentiveMessage;
						}
					}
					incomingUnreadMessages++;
					Message msg = getHandlerInstance().obtainMessage(UI_THREAD_MESSAGE_ON_UNREAD_INTERNAL, (IncomingTextMessage) apptentiveMessage);
					msg.sendToTarget();
				}
			}
			getMessageStore(appContext).addOrUpdateMessages(messagesToSave.toArray(new ApptentiveMessage[messagesToSave.size()]));
			Message msg;
			if (incomingUnreadMessages > 0) {
				// Show toast notification only if the forground activity is not alreay message center activity
				if (!forMessageCenter && showToast) {
					msg = getHandlerInstance().obtainMessage(UI_THREAD_MESSAGE_ON_TOAST_NOTIFICATION, messageOnToast);
					msg.sendToTarget();
				}
			}
			msg = getHandlerInstance().obtainMessage(UI_THREAD_MESSAGE_ON_UNREAD_HOST, getUnreadMessageCount(appContext), 0);
			msg.sendToTarget();

			return incomingUnreadMessages > 0;
		}
		return false;
	}

	public static List<MessageCenterUtil.MessageCenterListItem> getMessageCenterListItems(Context context) {
		List<MessageCenterUtil.MessageCenterListItem> messages = new ArrayList<>();
		messages.addAll(getMessageStore(context).getAllMessages());
		return messages;
	}

	public static void sendMessage(Context context, ApptentiveMessage apptentiveMessage) {
		getMessageStore(context).addOrUpdateMessages(apptentiveMessage);
		ApptentiveDatabase.getInstance(context).addPayload(apptentiveMessage);
	}

	/**
	 * This doesn't need to be run during normal program execution. Testing only.
	 */
	public static void deleteAllMessages(Context context) {
		Log.e("Deleting all messages.");
		getMessageStore(context).deleteAllMessages();
	}

	private static List<ApptentiveMessage> fetchMessages(Context appContext, String afterId) {
		Log.d("Fetching messages newer than: " + afterId);
		ApptentiveHttpResponse response = ApptentiveClient.getMessages(appContext, null, afterId, null);

		List<ApptentiveMessage> ret = new ArrayList<>();
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

	public static void updateMessage(Context context, ApptentiveMessage apptentiveMessage) {
		getMessageStore(context).updateMessage(apptentiveMessage);
	}

	public static List<ApptentiveMessage> parseMessagesString(String messageString) throws JSONException {
		List<ApptentiveMessage> ret = new ArrayList<>();
		JSONObject root = new JSONObject(messageString);
		if (!root.isNull("items")) {
			JSONArray items = root.getJSONArray("items");
			for (int i = 0; i < items.length(); i++) {
				String json = items.getJSONObject(i).toString();
				ApptentiveMessage apptentiveMessage = MessageFactory.fromJson(json);
				// Since these came back from the server, mark them saved before updating them in the DB.
				apptentiveMessage.setState(ApptentiveMessage.State.saved);
				ret.add(apptentiveMessage);
			}
		}
		return ret;
	}

	public static void onResumeSending() {
		if (afterSendMessageListener != null && afterSendMessageListener.get() != null) {
			afterSendMessageListener.get().onResumeSending();
		}
	}

	public static void onPauseSending(int reason_code) {
		if (afterSendMessageListener != null && afterSendMessageListener.get() != null) {
			afterSendMessageListener.get().onPauseSending(reason_code);
		}
	}

	public static void onSentMessage(Context context, ApptentiveMessage apptentiveMessage, ApptentiveHttpResponse response) {

		if (response.isRejectedPermanently() || response.isBadPayload()) {
			if (apptentiveMessage instanceof OutgoingFileMessage) {
				((OutgoingFileMessage) apptentiveMessage).deleteStoredFile(context);
			}
			return;
		}

		if (response.isRejectedTemporarily()) {
			onPauseSending(SEND_PAUSE_REASON_SERVER);
			return;
		}

		if (response.isSuccessful()) {
			// Don't store hidden messages once sent. Delete them.
			if (apptentiveMessage.isHidden()) {
				if (apptentiveMessage instanceof OutgoingFileMessage) {
					((OutgoingFileMessage) apptentiveMessage).deleteStoredFile(context);
				}
				getMessageStore(context).deleteMessage(apptentiveMessage.getNonce());
				return;
			}
			try {
				JSONObject responseJson = new JSONObject(response.getContent());
				if (apptentiveMessage.getState() == ApptentiveMessage.State.sending) {
					apptentiveMessage.setState(ApptentiveMessage.State.sent);
				}
				apptentiveMessage.setId(responseJson.getString(ApptentiveMessage.KEY_ID));
				apptentiveMessage.setCreatedAt(responseJson.getDouble(ApptentiveMessage.KEY_CREATED_AT));
			} catch (JSONException e) {
				Log.e("Error parsing sent apptentiveMessage response.", e);
			}
			getMessageStore(context).updateMessage(apptentiveMessage);

			if (afterSendMessageListener != null && afterSendMessageListener.get() != null) {
				afterSendMessageListener.get().onMessageSent(response, apptentiveMessage);
			}
		}
/*
		if(response.isBadPayload()) {
			// TODO: Tell the user that this apptentiveMessage failed to send. It will be deleted by the record send worker.
		}
*/
	}

	private static MessageStore getMessageStore(Context context) {
		return ApptentiveDatabase.getInstance(context);
	}

	public static int getUnreadMessageCount(Context context) {
		return getMessageStore(context).getUnreadMessageCount();
	}


	// Listeners
	public interface AfterSendMessageListener {
		void onMessageSent(ApptentiveHttpResponse response, ApptentiveMessage apptentiveMessage);

		void onPauseSending(int reason);

		void onResumeSending();
	}

	public static void setAfterSendMessageListener(AfterSendMessageListener listener) {
		if (listener != null) {
			afterSendMessageListener = new WeakReference<AfterSendMessageListener>(listener);
		} else {
			afterSendMessageListener = null;
		}
	}

	public interface OnNewIncomingMessagesListener {
		public void onMessagesUpdated(final IncomingTextMessage apptentiveMsg);
	}

	public static void addInternalOnMessagesUpdatedListener(OnNewIncomingMessagesListener newlistener) {
		if (newlistener != null) {
			for (Iterator<WeakReference<OnNewIncomingMessagesListener>> iterator = internalNewMessagesListeners.iterator(); iterator.hasNext(); ) {
				WeakReference<OnNewIncomingMessagesListener> listenerRef = iterator.next();
				OnNewIncomingMessagesListener listener = listenerRef.get();
				if (listener != null && listener == newlistener) {
					return;
				} else if (listener == null) {
					iterator.remove();
				}
			}
			internalNewMessagesListeners.add(new WeakReference<>(newlistener));
		}
	}

	public static void clearInternalOnMessagesUpdatedListeners() {
		internalNewMessagesListeners.clear();
	}

	public static void notifyInternalNewMessagesListeners(final IncomingTextMessage apptentiveMsg) {
		for (WeakReference<OnNewIncomingMessagesListener> listenerRef : internalNewMessagesListeners) {
			OnNewIncomingMessagesListener listener = listenerRef.get();
			if (listener != null) {
				listener.onMessagesUpdated(apptentiveMsg);
			}
		}
	}

	@Deprecated
	public static void setHostUnreadMessagesListener(UnreadMessagesListener newlistener) {
		if (newlistener != null) {
			clearHostUnreadMessagesListeners();
			hostUnreadMessagesListeners.add(new WeakReference<>(newlistener));
		}
	}

	public static void addHostUnreadMessagesListener(UnreadMessagesListener newlistener) {
		if (newlistener != null) {
			for (Iterator<WeakReference<UnreadMessagesListener>> iterator = hostUnreadMessagesListeners.iterator(); iterator.hasNext(); ) {
				WeakReference<UnreadMessagesListener> listenerRef = iterator.next();
				UnreadMessagesListener listener = listenerRef.get();
				if (listener != null && listener == newlistener) {
					return;
				} else if (listener == null) {
					iterator.remove();
				}
			}
			hostUnreadMessagesListeners.add(new WeakReference<>(newlistener));
		}
	}

	public static void clearHostUnreadMessagesListeners() {
		hostUnreadMessagesListeners.clear();
	}

	public static void notifyHostUnreadMessagesListeners(int unreadMessages) {
		for (WeakReference<UnreadMessagesListener> listenerRef : hostUnreadMessagesListeners) {
			UnreadMessagesListener listener = listenerRef.get();
			if (listener != null) {
				listener.onUnreadMessageCountChanged(unreadMessages);
			}
		}
	}


	// Set when an ApptentiveActivity onStart() is called
	public static void setCurrentForgroundActivity(Activity activity) {
		if (activity != null) {
			currentForgroundApptentiveActivity = new WeakReference<Activity>(activity);
		} else if (currentForgroundApptentiveActivity != null) {
			ApptentiveToastNotificationManager manager = ApptentiveToastNotificationManager.getInstance(currentForgroundApptentiveActivity.get(), false);
			if (manager != null) {
				manager.cleanUp();
			}
			currentForgroundApptentiveActivity = null;
		}
	}

	private static void showUnreadMessageToastNotification(final IncomingTextMessage apptentiveMsg) {
		if (currentForgroundApptentiveActivity != null && currentForgroundApptentiveActivity.get() != null) {
			Activity foreground = currentForgroundApptentiveActivity.get();
			if (foreground != null) {
				Intent intent;

				if (Apptentive.canShowMessageCenter(foreground.getApplicationContext())) {
					intent = new Intent();
					intent.setClass(foreground.getApplicationContext(), ViewActivity.class);
					intent.putExtra(ActivityContent.KEY, ActivityContent.Type.ENGAGE_INTERNAL_EVENT.name());
					intent.putExtra(ActivityContent.EVENT_NAME, MessageCenterInteraction.DEFAULT_INTERNAL_EVENT_NAME);
				} else {
					intent = MessageCenterInteraction.generateMessageCenterErrorIntent(foreground.getApplicationContext());
				}

				// TODO: Use a fallback intent if Message Center isn't yet available.

				PendingIntent pendingIntent = PendingIntent.getActivity(foreground.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				final ApptentiveToastNotificationManager manager = ApptentiveToastNotificationManager.getInstance(foreground, true);
				final ApptentiveToastNotification.Builder builder = new ApptentiveToastNotification.Builder(foreground);
				builder.setContentTitle(foreground.getResources().getString(R.string.apptentive_message_center_title))
						.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
						.setSmallIcon(R.drawable.avatar).setContentText(apptentiveMsg.getBody())
						.setContentIntent(pendingIntent)
						.setFullScreenIntent(pendingIntent, false);
				foreground.runOnUiThread(new Runnable() {
																	 public void run() {
																		 ApptentiveToastNotification notification = builder.buildApptentiveToastNotification();
																		 notification.setAvatarUrl(apptentiveMsg.getSenderProfilePhoto());
																		 manager.notify(TOAST_TYPE_UNREAD_MESSAGE, notification);
																	 }
																 }
				);
			}
		}
	}

}
