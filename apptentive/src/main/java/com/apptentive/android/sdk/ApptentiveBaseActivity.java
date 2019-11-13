package com.apptentive.android.sdk;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.conversation.ConversationDispatchTask;
import com.apptentive.android.sdk.debug.ErrorMetrics;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.notifications.ApptentiveNotification;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationObserver;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import static com.apptentive.android.sdk.ApptentiveHelper.dispatchConversationTask;
import static com.apptentive.android.sdk.ApptentiveNotifications.*;

/** A base class for any SDK activity */
public abstract class ApptentiveBaseActivity extends AppCompatActivity implements ApptentiveNotificationObserver {

	//region Activity life cycle

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerNotifications();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterNotification();
	}

	//endregion

	//region Notifications

	protected void registerNotifications() {
		ApptentiveNotificationCenter.defaultCenter()
			.addObserver(NOTIFICATION_INTERACTIONS_SHOULD_DISMISS, this)
			.addObserver(NOTIFICATION_CONVERSATION_STATE_DID_CHANGE, this);
	}

	protected void unregisterNotification() {
		ApptentiveNotificationCenter.defaultCenter().removeObserver(this);
	}

	//endregion

	//region ApptentiveNotificationObserver

	@Override
	public void onReceiveNotification(ApptentiveNotification notification) {
		// handle notification in subclasses
	}

	//endregion

	//region Helpers

	protected void logException(Exception e) {
		ErrorMetrics.logException(e); // TODO: add more context info
	}

	protected void engageInternal(final String eventName) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				return EngagementModule.engageInternal(ApptentiveBaseActivity.this, conversation, eventName);
			}
		}, "engage");
	}

	protected void dispatchOnMainQueue(DispatchTask task) {
		DispatchQueue.mainQueue().dispatchAsync(task);
	}

	//endregion
}
