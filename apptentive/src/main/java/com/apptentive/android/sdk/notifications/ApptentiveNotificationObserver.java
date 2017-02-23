package com.apptentive.android.sdk.notifications;

/**
 * Interface definition for a callback to be invoked when a notification is received.
 */
public interface ApptentiveNotificationObserver {
	/**
	 * Called when a view has been received.
	 */
	void onReceiveNotification(ApptentiveNotification notification);
}
