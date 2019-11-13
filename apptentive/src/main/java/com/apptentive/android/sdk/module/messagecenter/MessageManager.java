/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.comm.ApptentiveHttpClient;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.conversation.ConversationDispatchTask;
import com.apptentive.android.sdk.model.ApptentiveMessage;
import com.apptentive.android.sdk.model.CompoundMessage;
import com.apptentive.android.sdk.model.PayloadData;
import com.apptentive.android.sdk.model.PayloadType;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveToastNotification;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem;
import com.apptentive.android.sdk.module.messagecenter.model.MessageFactory;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.network.HttpJsonRequest;
import com.apptentive.android.sdk.network.HttpRequest;
import com.apptentive.android.sdk.notifications.ApptentiveNotification;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationObserver;
import com.apptentive.android.sdk.storage.MessageStore;
import com.apptentive.android.sdk.util.Destroyable;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.apptentive.android.sdk.ApptentiveHelper.checkConversationQueue;
import static com.apptentive.android.sdk.ApptentiveHelper.conversationQueue;
import static com.apptentive.android.sdk.ApptentiveHelper.dispatchOnConversationQueue;
import static com.apptentive.android.sdk.ApptentiveLogTag.MESSAGES;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_ACTIVITY_RESUMED;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_ACTIVITY_STARTED;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_APP_ENTERED_BACKGROUND;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_APP_ENTERED_FOREGROUND;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_ACTIVITY;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_MESSAGE_STORE;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_PAYLOAD;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_RESPONSE_CODE;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_RESPONSE_DATA;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_SUCCESSFUL;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_MESSAGE_STORE_DID_CHANGE;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_PAYLOAD_DID_FINISH_SEND;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_PAYLOAD_WILL_START_SEND;
import static com.apptentive.android.sdk.debug.Assert.assertNotNull;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class MessageManager implements Destroyable, ApptentiveNotificationObserver {

	// The reason of pause message sending
	public static int SEND_PAUSE_REASON_ACTIVITY_PAUSE = 0;
	public static int SEND_PAUSE_REASON_NETWORK = 1;
	public static int SEND_PAUSE_REASON_SERVER = 2;

	private static int TOAST_TYPE_UNREAD_MESSAGE = 1;

	private final Conversation conversation;

	private final MessageStore messageStore;

	private WeakReference<Activity> currentForegroundApptentiveActivity;

	private WeakReference<AfterSendMessageListener> afterSendMessageListener;

	private final List<WeakReference<OnNewIncomingMessagesListener>> internalNewMessagesListeners = new ArrayList<>();

	/* UnreadMessagesListener is set by external hosting app, and its lifecycle is managed by the app.
	 * Use WeakReference to prevent memory leak
	 */
	private final List<WeakReference<UnreadMessagesListener>> hostUnreadMessagesListeners = new ArrayList<>();

	private final AtomicBoolean appInForeground = new AtomicBoolean(false); // TODO: get rid of that
	private final MessagePollingWorker pollingWorker;

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

	/**
	 * Testing only.
	 */
	protected MessageManager() {
		conversation = null;
		messageStore = null;
		pollingWorker = null;
	}

	public MessageManager(Conversation conversation, MessageStore messageStore) {
		if (conversation == null) {
			throw new IllegalArgumentException("Conversation is null");
		}

		if (messageStore == null) {
			throw new IllegalArgumentException("Message store is null");
		}

		this.conversation = conversation;
		this.messageStore = new MessageStoreObserver(messageStore);
		this.pollingWorker = new MessagePollingWorker(this);

		registerNotifications();
	}

	/*
	 * Starts an AsyncTask to pre-fetch messages. This is to be called as part of Push notification action
	 * when push is received on the device.
	 */
	public void startMessagePreFetchTask() {
		try {
			boolean updateMC = isMessageCenterInForeground();
			fetchAndStoreMessages(updateMC, false, null);
		} catch (final Exception e) {
			ApptentiveLog.w(MESSAGES, e, "Unhandled Exception thrown from fetching new message task");
			logException(e);
		}
	}

	/**
	 * Performs a request against the server to check for messages in the conversation since the latest message we already have.
	 * This method will either be run on MessagePollingThread or as an asyncTask when Push is received.
	 */
	void fetchAndStoreMessages(final boolean isMessageCenterForeground, final boolean showToast, @Nullable final MessageFetchListener listener) {
		checkConversationQueue();

		try {
			String lastMessageId = messageStore.getLastReceivedMessageId();
			fetchMessages(lastMessageId, new MessageFetchListener() {
				@Override
				public void onFetchFinish(MessageManager messageManager, List<ApptentiveMessage> messages) {
					try {
						if (messages == null || messages.size() == 0) return;

						CompoundMessage messageOnToast = null;
						ApptentiveLog.d(MESSAGES,"Messages retrieved: %d", messages.size());

						// Also get the count of incoming unread messages.
						int incomingUnreadMessages = 0;
						// Mark messages from server where sender is the app user as read.
						for (final ApptentiveMessage apptentiveMessage : messages) {
							if (apptentiveMessage.isOutgoingMessage()) {
								apptentiveMessage.setRead(true);
							} else {
								if (messageOnToast == null) {
									if (apptentiveMessage.getMessageType() == ApptentiveMessage.Type.CompoundMessage) {
										messageOnToast = (CompoundMessage) apptentiveMessage;
									}
								}
								incomingUnreadMessages++;

								// for every new message received, notify Message Center
								notifyInternalNewMessagesListeners((CompoundMessage) apptentiveMessage);
							}
						}
						messageStore.addOrUpdateMessages(messages.toArray(new ApptentiveMessage[messages.size()]));
						if (incomingUnreadMessages > 0) {
							// Show toast notification only if the foreground activity is not already message center activity
							if (!isMessageCenterForeground && showToast) {
								DispatchQueue.mainQueue().dispatchAsyncOnce(toastMessageNotifierTask.setMessage(messageOnToast));
							}
						}

						// Send message to notify host app, such as unread message badge
						conversationQueue().dispatchAsyncOnce(hostMessageNotifierTask.setMessageCount(getUnreadMessageCount()));
					} finally {
						if (listener != null) {
							listener.onFetchFinish(messageManager, messages);
						}
					}
				}
			});
		} catch (Exception e) {
			ApptentiveLog.e(MESSAGES, "Error retrieving last received message id from worker thread");
			logException(e);
		}
	}

	public List<MessageCenterListItem> getMessageCenterListItems() {
		List<MessageCenterListItem> messagesToShow = new ArrayList<>();
		try {
			List<ApptentiveMessage> messagesAll = messageStore.getAllMessages();
			// Do not display hidden messages on Message Center
			for (ApptentiveMessage message : messagesAll) {
				if (!message.isHidden()) {
					messagesToShow.add(message);
				}
			}
		} catch (Exception e) {
			ApptentiveLog.e(MESSAGES,"Error getting all messages in worker thread");
			logException(e);
		}

		return messagesToShow;
	}

	public void sendMessage(ApptentiveMessage apptentiveMessage) {
		messageStore.addOrUpdateMessages(apptentiveMessage);
		conversation.addPayload(apptentiveMessage);
	}

	public void addMessages(ApptentiveMessage[] messages) {
		messageStore.addOrUpdateMessages(messages);
	}

	/**
	 * This doesn't need to be run during normal program execution. Testing only.
	 */
	public void deleteAllMessages(Context context) {
		ApptentiveLog.d(MESSAGES, "Deleting all messages.");
		messageStore.deleteAllMessages();
	}

	private HttpJsonRequest fetchMessages(String afterId, final MessageFetchListener listener) {
		ApptentiveLog.v(MESSAGES, "Fetching messages newer than: %s", (afterId == null) ? "0" : afterId);

		// TODO: Use the new ApptentiveHttpClient for this.
		ApptentiveHttpClient httpClient = ApptentiveInternal.getInstance().getApptentiveHttpClient();
		HttpJsonRequest request = httpClient.createFetchMessagesRequest(conversation.getConversationToken(), conversation.getConversationId(), afterId, null, null, new HttpRequest.Listener<HttpJsonRequest>() {
			@Override
			public void onFinish(HttpJsonRequest request) {
				try {
					notifyFinished(listener, parseMessages(request.getResponseObject()));
				} catch (Exception e) {
					ApptentiveLog.e(MESSAGES, e, "Exception while parsing messages");
					logException(e);

					notifyFinished(listener, null);
				}
			}

			@Override
			public void onCancel(HttpJsonRequest request) {
			}

			@Override
			public void onFail(HttpJsonRequest request, String reason) {
				ApptentiveLog.e(MESSAGES, "Error while fetching messages: %s", reason);
				notifyFinished(listener, null);
			}

			private void notifyFinished(MessageFetchListener listener, List<ApptentiveMessage> messages) {
				if (listener != null) {
					listener.onFetchFinish(MessageManager.this, messages);
				}
			}
		});
		request.setCallbackQueue(conversationQueue());
		request.start();
		return request;
	}

	public void updateMessage(ApptentiveMessage apptentiveMessage) {
		messageStore.updateMessage(apptentiveMessage);
	}

	public List<ApptentiveMessage> parseMessages(JSONObject root) throws JSONException {
		List<ApptentiveMessage> ret = new ArrayList<>();
		if (!root.isNull("messages")) {
			JSONArray items = root.getJSONArray("messages");
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

	public void onSentMessage(String nonce, int responseCode, JSONObject responseJson) {

		final ApptentiveMessage apptentiveMessage = messageStore.findMessage(nonce);
		assertNotNull(apptentiveMessage, "Can't find a message with nonce: %s", nonce);
		if (apptentiveMessage == null) {
			return; // should not happen but we want to stay safe
		}

		final boolean isRejectedPermanently = responseCode >= 400 && responseCode < 500;
		final boolean isSuccessful = responseCode >= 200 && responseCode < 300;
		final boolean isRejectedTemporarily = !(isSuccessful || isRejectedPermanently);

		if (isRejectedPermanently || responseCode == -1) {
			if (apptentiveMessage instanceof CompoundMessage) {
				apptentiveMessage.setCreatedAt(Double.MIN_VALUE);
				messageStore.updateMessage(apptentiveMessage);
				if (afterSendMessageListener != null && afterSendMessageListener.get() != null) {
					afterSendMessageListener.get().onMessageSent(responseCode, apptentiveMessage);
				}

			}
			return;
		}

		if (isRejectedTemporarily) {
			pauseSending(SEND_PAUSE_REASON_SERVER);
			return;
		}

		if (isSuccessful) {
			assertNotNull(responseJson, "Missing required responseJson.");
			// Don't store hidden messages once sent. Delete them.
			if (apptentiveMessage.isHidden()) {
				((CompoundMessage) apptentiveMessage).deleteAssociatedFiles();
				messageStore.deleteMessage(apptentiveMessage.getNonce());
				return;
			}
			try {
				apptentiveMessage.setState(ApptentiveMessage.State.sent);
				apptentiveMessage.setId(responseJson.getString(ApptentiveMessage.KEY_ID));
				apptentiveMessage.setCreatedAt(responseJson.getDouble(ApptentiveMessage.KEY_CREATED_AT));
			} catch (JSONException e) {
				ApptentiveLog.e(MESSAGES, e, "Error parsing sent apptentiveMessage response.");
				logException(e);
			}
			messageStore.updateMessage(apptentiveMessage);

			if (afterSendMessageListener != null && afterSendMessageListener.get() != null) {
				afterSendMessageListener.get().onMessageSent(responseCode, apptentiveMessage);
			}
		}
	}

	public int getUnreadMessageCount() {
		int msgCount = 0;
		try {
			msgCount = messageStore.getUnreadMessageCount();
		} catch (Exception e) {
			ApptentiveLog.e(MESSAGES, "Error getting unread messages count in worker thread");
			logException(e);
		}
		return msgCount;
	}

	//region Notifications

	private void registerNotifications() {
		ApptentiveNotificationCenter.defaultCenter()
			.addObserver(NOTIFICATION_ACTIVITY_STARTED, this)
			.addObserver(NOTIFICATION_ACTIVITY_RESUMED, this)
			.addObserver(NOTIFICATION_APP_ENTERED_FOREGROUND, this)
			.addObserver(NOTIFICATION_APP_ENTERED_BACKGROUND, this)
			.addObserver(NOTIFICATION_PAYLOAD_WILL_START_SEND, this)
			.addObserver(NOTIFICATION_PAYLOAD_DID_FINISH_SEND, this);
	}

	//endregion

	//region Notification Observer

	@Override
	public void onReceiveNotification(ApptentiveNotification notification) {
		checkConversationQueue();

		if (notification.hasName(NOTIFICATION_ACTIVITY_STARTED) ||
			notification.hasName(NOTIFICATION_ACTIVITY_RESUMED)) {
			final Activity activity = notification.getRequiredUserInfo(NOTIFICATION_KEY_ACTIVITY, Activity.class);
			setCurrentForegroundActivity(activity);
		} else if (notification.hasName(NOTIFICATION_APP_ENTERED_FOREGROUND)) {
			appWentToForeground();
		} else if (notification.hasName(NOTIFICATION_APP_ENTERED_BACKGROUND)) {
			setCurrentForegroundActivity(null);
			appWentToBackground();
		} else if (notification.hasName(NOTIFICATION_PAYLOAD_WILL_START_SEND)) {
			final PayloadData payload = notification.getRequiredUserInfo(NOTIFICATION_KEY_PAYLOAD, PayloadData.class);
			if (payload.getType().equals(PayloadType.message)) {
				resumeSending();
			}
		} else if (notification.hasName(NOTIFICATION_PAYLOAD_DID_FINISH_SEND)) {
			final boolean successful = notification.getRequiredUserInfo(NOTIFICATION_KEY_SUCCESSFUL, Boolean.class);
			final PayloadData payload = notification.getRequiredUserInfo(NOTIFICATION_KEY_PAYLOAD, PayloadData.class);
			final Integer responseCode = notification.getRequiredUserInfo(NOTIFICATION_KEY_RESPONSE_CODE, Integer.class);
			final JSONObject responseData = successful ? notification.getRequiredUserInfo(NOTIFICATION_KEY_RESPONSE_DATA, JSONObject.class) : null;
			if (responseCode == -1) {
				pauseSending(SEND_PAUSE_REASON_NETWORK);
			}

			if (payload.getType().equals(PayloadType.message)) {
				onSentMessage(payload.getNonce(), responseCode, responseData);
			}
		}
	}

	//endregion

	//region Destroyable

	@Override
	public void destroy() {
		ApptentiveNotificationCenter.defaultCenter().removeObserver(this);
		pollingWorker.destroy();
	}

	//endregion

	//region Polling

	public void attemptToStartMessagePolling() {
		if (conversation.isMessageCenterFeatureUsed()) {
			pollingWorker.startPolling();
		}
	}

	public void stopPollingMessages() {
		pollingWorker.stopPolling();
	}

	//endregion

	// Listeners
	public interface AfterSendMessageListener {
		void onMessageSent(int responseCode, ApptentiveMessage apptentiveMessage);

		void onPauseSending(int reason);

		void onResumeSending();
	}

	public interface OnNewIncomingMessagesListener {
		void onNewMessageReceived(final CompoundMessage apptentiveMsg);
	}

	public void setAfterSendMessageListener(AfterSendMessageListener listener) {
		if (listener != null) {
			afterSendMessageListener = new WeakReference<>(listener);
		} else {
			afterSendMessageListener = null;
		}
	}


	public void addInternalOnMessagesUpdatedListener(OnNewIncomingMessagesListener newlistener) {
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

	public void clearInternalOnMessagesUpdatedListeners() {
		internalNewMessagesListeners.clear();
	}

	private void notifyInternalNewMessagesListeners(final CompoundMessage apptentiveMsg) {
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
			// Defer message polling thread creation, if not created yet, and host app adds an unread message listenerinit();
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

	private void clearHostUnreadMessagesListeners() {
		hostUnreadMessagesListeners.clear();
	}

	public void notifyHostUnreadMessagesListeners(final int unreadMessages) {
		checkConversationQueue();

		// we dispatch listeners on the main queue
		for (WeakReference<UnreadMessagesListener> listenerRef : hostUnreadMessagesListeners) {
			final UnreadMessagesListener listener = listenerRef.get();
			if (listener != null) {
				DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
					@Override
					protected void execute() {
						listener.onUnreadMessageCountChanged(unreadMessages);
					}
				});
			}
		}
	}

	// Set when Activity.onStart() and onStop() are called
	private void setCurrentForegroundActivity(Activity activity) {
		if (activity != null) {
			currentForegroundApptentiveActivity = new WeakReference<>(activity);
		} else {
			ApptentiveToastNotificationManager manager = ApptentiveToastNotificationManager.getInstance(null, false);
			if (manager != null) {
				manager.cleanUp();
			}
			currentForegroundApptentiveActivity = null;
		}
	}

	public void setMessageCenterInForeground(boolean bInForeground) {
		conversation.setMessageCenterFeatureUsed(true);
		pollingWorker.setMessageCenterInForeground(bInForeground);
	}

	private boolean isMessageCenterInForeground() {
		return pollingWorker.isMessageCenterInForeground();
	}

	private void showUnreadMessageToastNotification(final CompoundMessage apptentiveMsg) {
		if (currentForegroundApptentiveActivity != null && currentForegroundApptentiveActivity.get() != null) {
			final Activity foreground = currentForegroundApptentiveActivity.get();
			if (foreground != null) {
				dispatchOnConversationQueue(new ConversationDispatchTask() {
					@Override
					protected boolean execute(Conversation conversation) {
						PendingIntent pendingIntent = ApptentiveInternal.prepareMessageCenterPendingIntent(foreground.getApplicationContext(), conversation);
						if (pendingIntent != null) {
							final ApptentiveToastNotificationManager manager = ApptentiveToastNotificationManager.getInstance(foreground, true);
							final ApptentiveToastNotification.Builder builder = new ApptentiveToastNotification.Builder(foreground);
							builder.setContentTitle(foreground.getResources().getString(R.string.apptentive_message_center_title))
									.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
									.setSmallIcon(R.drawable.avatar).setContentText(apptentiveMsg.getBody())
									.setContentIntent(pendingIntent)
									.setFullScreenIntent(pendingIntent, false);
							DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
								@Override
								protected void execute() {
									ApptentiveToastNotification notification = builder.buildApptentiveToastNotification();
									notification.setAvatarUrl(apptentiveMsg.getSenderProfilePhoto());
									manager.notify(TOAST_TYPE_UNREAD_MESSAGE, notification);}
								});
						}

						return true;
					}
				});
			}
		}
	}

	private void appWentToForeground() {
		appInForeground.set(true);
		if (conversation.isMessageCenterFeatureUsed()) {
			pollingWorker.appWentToForeground();
		}
	}

	private void appWentToBackground() {
		appInForeground.set(false);
		pollingWorker.appWentToBackground();
	}

	Conversation getConversation() {
		return conversation;
	}

	public MessageStore getMessageStore() {
		return messageStore;
	}

	public interface MessageFetchListener {
		void onFetchFinish(MessageManager messageManager, List<ApptentiveMessage> messages);
	}

	//region Message Dispatch Task

	private abstract static class MessageDispatchTask extends DispatchTask {
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

	private abstract static class MessageCountDispatchTask extends DispatchTask {
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

	//region Message Store Observer

	/**
	 * A wrapper class which sends notifications every time a target message store changes
	 */
	private static class MessageStoreObserver implements MessageStore {

		private final MessageStore target;

		private MessageStoreObserver(MessageStore target) {
			if (target == null) {
				throw new IllegalArgumentException("Target is null");
			}
			this.target = target;
		}

		@Override
		public void addOrUpdateMessages(ApptentiveMessage... messages) {
			target.addOrUpdateMessages(messages);
			notifyChanged();
		}

		@Override
		public void updateMessage(ApptentiveMessage message) {
			target.updateMessage(message);
			notifyChanged();
		}

		@Override
		public List<ApptentiveMessage> getAllMessages() throws Exception {
			return target.getAllMessages();
		}

		@Override
		public String getLastReceivedMessageId() throws Exception {
			return target.getLastReceivedMessageId();
		}

		@Override
		public int getUnreadMessageCount() throws Exception {
			return target.getUnreadMessageCount();
		}

		@Override
		public void deleteAllMessages() {
			target.deleteAllMessages();
			notifyChanged();
		}

		@Override
		public void deleteMessage(String nonce) {
			target.deleteMessage(nonce);
			notifyChanged();
		}

		@Override
		public ApptentiveMessage findMessage(String nonce) {
			return target.findMessage(nonce);
		}

		//region Notifications

		private void notifyChanged() {
			ApptentiveNotificationCenter.defaultCenter()
					.postNotification(NOTIFICATION_MESSAGE_STORE_DID_CHANGE, NOTIFICATION_KEY_MESSAGE_STORE, this);
		}

		//endregion
	}

	//endregion
}
