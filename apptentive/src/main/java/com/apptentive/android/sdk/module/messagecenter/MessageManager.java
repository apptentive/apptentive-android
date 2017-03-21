/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveToastNotification;
import com.apptentive.android.sdk.module.messagecenter.model.CompoundMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem;
import com.apptentive.android.sdk.module.messagecenter.model.MessageFactory;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.storage.MessageStore;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageManager {

	// The reason of pause message sending
	public static int SEND_PAUSE_REASON_ACTIVITY_PAUSE = 0;
	public static int SEND_PAUSE_REASON_NETWORK = 1;
	public static int SEND_PAUSE_REASON_SERVER = 2;

	private static int TOAST_TYPE_UNREAD_MESSAGE = 1;

	private WeakReference<Activity> currentForegroundApptentiveActivity;

	private WeakReference<AfterSendMessageListener> afterSendMessageListener;

	private final List<WeakReference<OnNewIncomingMessagesListener>> internalNewMessagesListeners = new ArrayList<WeakReference<OnNewIncomingMessagesListener>>();


	/* UnreadMessagesListener is set by external hosting app, and its lifecycle is managed by the app.
	 * Use WeakReference to prevent memory leak
	 */
	private final List<WeakReference<UnreadMessagesListener>> hostUnreadMessagesListeners = new ArrayList<WeakReference<UnreadMessagesListener>>();

	AtomicBoolean appInForeground = new AtomicBoolean(false);
	private Handler uiHandler;
	private MessagePollingWorker pollingWorker;

	private final MessageDispatchTask toastMessageNotifierTask = new MessageDispatchTask() {
		@Override
		protected void execute(CompoundMessage message) {
			showUnreadMessageToastNotification(message);
		}
	};

	private final MessageCountDispatchTask hostMessageNotifierTask = new MessageCountDispatchTask() {
		@Override
		protected void execute(int messageCount) {
			notifyHostUnreadMessagesListeners(messageCount);
		}
	};

	public MessageManager() {

	}

	// init() will start polling worker.
	public void init() {
		if (uiHandler == null) {
			uiHandler = new Handler(Looper.getMainLooper()) {
				@Override
				public void handleMessage(android.os.Message msg) {
					switch (msg.what) {
						case UI_THREAD_MESSAGE_ON_TOAST_NOTIFICATION: {
							CompoundMessage msgToShow = (CompoundMessage) msg.obj;
							showUnreadMessageToastNotification(msgToShow);
							break;
						}
						default:
							super.handleMessage(msg);
					}
				}
			};
		}
		if (pollingWorker == null) {
			pollingWorker = new MessagePollingWorker(this);
			/* Set SharePreference to indicate Message Center feature is desired. It will always be checked
			 * during Apptentive initialization.
			 */
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				conversation.setMessageCenterFeatureUsed(true);
			}
		}
	}

	/*
	 * Starts an AsyncTask to pre-fetch messages. This is to be called as part of Push notification action
	 * when push is received on the device.
	 */
	public void startMessagePreFetchTask() {
		// Defer message polling thread creation, if not created yet and host app receives a new message push
		init();
		AsyncTask<Object, Void, Void> task = new AsyncTask<Object, Void, Void>() {
			private Exception e = null;

			@Override
			protected Void doInBackground(Object... params) {
				boolean updateMC = (Boolean) params[0];
				try {
					fetchAndStoreMessages(updateMC, false);
				} catch (Exception e) {
					this.e = e;
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void v) {
				if (e != null) {
					ApptentiveLog.w("Unhandled Exception thrown from fetching new message asyncTask", e);
					MetricModule.sendError(e, null, null);
				}
			}
		};

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, isMessageCenterInForeground());
		} else {
			task.execute(isMessageCenterInForeground());
		}
	}

	/**
	 * Performs a request against the server to check for messages in the conversation since the latest message we already have.
	 * This method will either be run on MessagePollingThread or as an asyncTask when Push is received.
	 *
	 * @return true if messages were returned, else false.
	 */
	public synchronized boolean fetchAndStoreMessages(boolean isMessageCenterForeground, boolean showToast) {
		if (ApptentiveInternal.getInstance().getConversation().getConversationToken() == null) {
			ApptentiveLog.d("Can't fetch messages because the conversation has not yet been initialized.");
			return false;
		}
		if (!Util.isNetworkConnectionPresent()) {
			ApptentiveLog.d("Can't fetch messages because a network connection is not present.");
			return false;
		}

		// Fetch the messages.
		List<ApptentiveMessage> messagesToSave = null;
		try {
			Future<String> future = getMessageStore().getLastReceivedMessageId();
			messagesToSave = fetchMessages(future.get());
		} catch (Exception e) {
			ApptentiveLog.e("Error retrieving last received message id from worker thread");
		}

		CompoundMessage messageOnToast = null;
		if (messagesToSave != null && messagesToSave.size() > 0) {
			ApptentiveLog.d("Messages retrieved.");
			// Also get the count of incoming unread messages.
			int incomingUnreadMessages = 0;
			// Mark messages from server where sender is the app user as read.
			for (final ApptentiveMessage apptentiveMessage : messagesToSave) {
				if (apptentiveMessage.isOutgoingMessage()) {
					apptentiveMessage.setRead(true);
				} else {
					if (messageOnToast == null) {
						if (apptentiveMessage.getType() == ApptentiveMessage.Type.CompoundMessage) {
							messageOnToast = (CompoundMessage) apptentiveMessage;
						}
					}
					incomingUnreadMessages++;

					// for every new message received, notify Message Center
					DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
						@Override
						protected void execute() {
							notifyInternalNewMessagesListeners((CompoundMessage) apptentiveMessage);
						}
					});
				}
			}
			getMessageStore().addOrUpdateMessages(messagesToSave.toArray(new ApptentiveMessage[messagesToSave.size()]));
			if (incomingUnreadMessages > 0) {
				// Show toast notification only if the foreground activity is not already message center activity
				if (!isMessageCenterForeground && showToast) {
					DispatchQueue.mainQueue().dispatchAsyncOnce(toastMessageNotifierTask.setMessage(messageOnToast));
				}
			}

			// Send message to notify host app, such as unread message badge
			DispatchQueue.mainQueue()
					.dispatchAsyncOnce(hostMessageNotifierTask.setMessageCount(getUnreadMessageCount()));

			return incomingUnreadMessages > 0;
		}
		return false;
	}

	public List<MessageCenterListItem> getMessageCenterListItems() {
		List<MessageCenterListItem> messagesToShow = new ArrayList<MessageCenterListItem>();
		try {
			List<ApptentiveMessage> messagesAll = getMessageStore().getAllMessages().get();
			// Do not display hidden messages on Message Center
			for (ApptentiveMessage message : messagesAll) {
				if (!message.isHidden()) {
					messagesToShow.add(message);
				}
			}
		} catch (Exception e) {
			ApptentiveLog.e("Error getting all messages in worker thread");
		}

		return messagesToShow;
	}

	public void sendMessage(ApptentiveMessage apptentiveMessage) {
		getMessageStore().addOrUpdateMessages(apptentiveMessage);
		ApptentiveInternal.getInstance().getApptentiveTaskManager().addPayload(apptentiveMessage);
	}

	/**
	 * This doesn't need to be run during normal program execution. Testing only.
	 */
	public void deleteAllMessages(Context context) {
		ApptentiveLog.d("Deleting all messages.");
		getMessageStore().deleteAllMessages();
	}

	private List<ApptentiveMessage> fetchMessages(String afterId) {
		ApptentiveLog.d("Fetching messages newer than: %s", (afterId == null) ? "0" : afterId);

		ApptentiveHttpResponse response = ApptentiveClient.getMessages(null, afterId, null);

		List<ApptentiveMessage> ret = new ArrayList<ApptentiveMessage>();
		if (!response.isSuccessful()) {
			return ret;
		}
		try {
			ret = parseMessagesString(response.getContent());
		} catch (JSONException e) {
			ApptentiveLog.e("Error parsing messages JSON.", e);
		} catch (Exception e) {
			ApptentiveLog.e("Unexpected error parsing messages JSON.", e);
		}
		return ret;
	}

	public void updateMessage(ApptentiveMessage apptentiveMessage) {
		getMessageStore().updateMessage(apptentiveMessage);
	}

	public List<ApptentiveMessage> parseMessagesString(String messageString) throws JSONException {
		List<ApptentiveMessage> ret = new ArrayList<ApptentiveMessage>();
		JSONObject root = new JSONObject(messageString);
		if (!root.isNull("items")) {
			JSONArray items = root.getJSONArray("items");
			for (int i = 0; i < items.length(); i++) {
				String json = items.getJSONObject(i).toString();
				ApptentiveMessage apptentiveMessage = MessageFactory.fromJson(json);
				// Since these came back from the server, mark them saved before updating them in the DB.
				if (apptentiveMessage != null) {
					apptentiveMessage.setState(ApptentiveMessage.State.saved);
					ret.add(apptentiveMessage);
				}
			}
		}
		return ret;
	}

	public void resumeSending() {
		if (afterSendMessageListener != null && afterSendMessageListener.get() != null) {
			afterSendMessageListener.get().onResumeSending();
		}
	}

	public void pauseSending(int reason_code) {
		if (afterSendMessageListener != null && afterSendMessageListener.get() != null) {
			afterSendMessageListener.get().onPauseSending(reason_code);
		}
	}

	public void onSentMessage(ApptentiveMessage apptentiveMessage, ApptentiveHttpResponse response) {

		if (response.isRejectedPermanently() || response.isBadPayload()) {
			if (apptentiveMessage instanceof CompoundMessage) {
				apptentiveMessage.setCreatedAt(Double.MIN_VALUE);
				getMessageStore().updateMessage(apptentiveMessage);
				if (afterSendMessageListener != null && afterSendMessageListener.get() != null) {
					afterSendMessageListener.get().onMessageSent(response, apptentiveMessage);
				}

			}
			return;
		}

		if (response.isRejectedTemporarily()) {
			pauseSending(SEND_PAUSE_REASON_SERVER);
			return;
		}

		if (response.isSuccessful()) {
			// Don't store hidden messages once sent. Delete them.
			if (apptentiveMessage.isHidden()) {
				((CompoundMessage) apptentiveMessage).deleteAssociatedFiles();
				getMessageStore().deleteMessage(apptentiveMessage.getNonce());
				return;
			}
			try {
				JSONObject responseJson = new JSONObject(response.getContent());

				apptentiveMessage.setState(ApptentiveMessage.State.sent);

				apptentiveMessage.setId(responseJson.getString(ApptentiveMessage.KEY_ID));
				apptentiveMessage.setCreatedAt(responseJson.getDouble(ApptentiveMessage.KEY_CREATED_AT));
			} catch (JSONException e) {
				ApptentiveLog.e("Error parsing sent apptentiveMessage response.", e);
			}
			getMessageStore().updateMessage(apptentiveMessage);

			if (afterSendMessageListener != null && afterSendMessageListener.get() != null) {
				afterSendMessageListener.get().onMessageSent(response, apptentiveMessage);
			}
		}
	}

	private MessageStore getMessageStore() {
		return ApptentiveInternal.getInstance().getApptentiveTaskManager();
	}

	public int getUnreadMessageCount() {
		int msgCount = 0;
		try {
			msgCount = getMessageStore().getUnreadMessageCount().get();
		} catch (Exception e) {
			ApptentiveLog.e("Error getting unread messages count in worker thread");
		}
		return msgCount;
	}


	// Listeners
	public interface AfterSendMessageListener {
		void onMessageSent(ApptentiveHttpResponse response, ApptentiveMessage apptentiveMessage);

		void onPauseSending(int reason);

		void onResumeSending();
	}

	public interface OnNewIncomingMessagesListener {
		void onNewMessageReceived(final CompoundMessage apptentiveMsg);
	}

	public void setAfterSendMessageListener(AfterSendMessageListener listener) {
		if (listener != null) {
			afterSendMessageListener = new WeakReference<AfterSendMessageListener>(listener);
		} else {
			afterSendMessageListener = null;
		}
	}


	public void addInternalOnMessagesUpdatedListener(OnNewIncomingMessagesListener newlistener) {
		if (newlistener != null) {
			init();
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

	public void clearInternalOnMessagesUpdatedListeners() {
		internalNewMessagesListeners.clear();
	}

	public void notifyInternalNewMessagesListeners(final CompoundMessage apptentiveMsg) {
		for (WeakReference<OnNewIncomingMessagesListener> listenerRef : internalNewMessagesListeners) {
			OnNewIncomingMessagesListener listener = listenerRef.get();
			if (listener != null) {
				listener.onNewMessageReceived(apptentiveMsg);
			}
		}
	}

	@Deprecated
	public void setHostUnreadMessagesListener(UnreadMessagesListener listener) {
		clearHostUnreadMessagesListeners();
		if (listener != null) {
			hostUnreadMessagesListeners.add(new WeakReference<>(listener));
		}
	}

	public void addHostUnreadMessagesListener(UnreadMessagesListener newListener) {
		if (newListener != null) {
			// Defer message polling thread creation, if not created yet, and host app adds an unread message listener
			init();
			for (Iterator<WeakReference<UnreadMessagesListener>> iterator = hostUnreadMessagesListeners.iterator(); iterator.hasNext(); ) {
				WeakReference<UnreadMessagesListener> listenerRef = iterator.next();
				UnreadMessagesListener listener = listenerRef.get();
				if (listener != null && listener == newListener) {
					return;
				} else if (listener == null) {
					iterator.remove();
				}
			}
			hostUnreadMessagesListeners.add(new WeakReference<>(newListener));
		}
	}

	public void clearHostUnreadMessagesListeners() {
		hostUnreadMessagesListeners.clear();
	}

	public void notifyHostUnreadMessagesListeners(int unreadMessages) {
		for (WeakReference<UnreadMessagesListener> listenerRef : hostUnreadMessagesListeners) {
			UnreadMessagesListener listener = listenerRef.get();
			if (listener != null) {
				listener.onUnreadMessageCountChanged(unreadMessages);
			}
		}
	}

	// Set when Activity.onStart() and onStop() are called
	public void setCurrentForegroundActivity(Activity activity) {
		if (activity != null) {
			currentForegroundApptentiveActivity = new WeakReference<Activity>(activity);
		} else {
			ApptentiveToastNotificationManager manager = ApptentiveToastNotificationManager.getInstance(null, false);
			if (manager != null) {
				manager.cleanUp();
			}
			currentForegroundApptentiveActivity = null;
		}
	}

	public void setMessageCenterInForeground(boolean bInForeground) {
		pollingWorker.setMessageCenterInForeground(bInForeground);
	}

	public boolean isMessageCenterInForeground() {
		return pollingWorker.messageCenterInForeground.get();
	}

	private void showUnreadMessageToastNotification(final CompoundMessage apptentiveMsg) {
		if (currentForegroundApptentiveActivity != null && currentForegroundApptentiveActivity.get() != null) {
			Activity foreground = currentForegroundApptentiveActivity.get();
			if (foreground != null) {
				PendingIntent pendingIntent = ApptentiveInternal.prepareMessageCenterPendingIntent(foreground.getApplicationContext());
				if (pendingIntent != null) {
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

	public void appWentToForeground() {
		appInForeground.set(true);
		if (pollingWorker != null) {
			pollingWorker.appWentToForeground();
		}
	}

	public void appWentToBackground() {
		appInForeground.set(false);
		if (pollingWorker != null) {
			pollingWorker.appWentToBackground();
		}
	}

	//region Message Dispatch Task

	private abstract static class MessageDispatchTask extends DispatchTask
	{
		private CompoundMessage message;

		protected abstract void execute(CompoundMessage message);

		@Override
		protected void execute() {
			try {
				execute(message);
			} finally {
				message = null;
			}
		}

		MessageDispatchTask setMessage(CompoundMessage message) {
			this.message = message;
			return this;
		}
	}

	private abstract static class MessageCountDispatchTask extends DispatchTask
	{
		private int messageCount;

		protected abstract void execute(int messageCount);

		@Override
		protected void execute() {
			execute(messageCount);
		}

		MessageCountDispatchTask setMessageCount(int messageCount) {
			this.messageCount = messageCount;
			return this;
		}
	}

	//endregion
}
