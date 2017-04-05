/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

public class ApptentiveNotifications {

	/**
	 * Sent when conversation state changes (user logs out, etc)
	 */
	public static final String NOTIFICATION_CONVERSATION_STATE_DID_CHANGE = "CONVERSATION_STATE_DID_CHANGE"; // { conversation : Conversation }

	/**
	 * Sent if a new activity is started.
	 */
	public static final String NOTIFICATION_ACTIVITY_STARTED = "NOTIFICATION_ACTIVITY_STARTED"; // { activity : Activity }

	/**
	 * Sent if activity is resumed.
	 */
	public static final String NOTIFICATION_ACTIVITY_RESUMED = "NOTIFICATION_ACTIVITY_RESUMED"; // { activity : Activity }

	/**
	 * Sent if app enter foreground
	 */
	public static final String NOTIFICATION_APP_ENTER_FOREGROUND = "NOTIFICATION_APP_ENTER_FOREGROUND";

	/**
	 * Sent if app enter background
	 */
	public static final String NOTIFICATION_APP_ENTER_BACKGROUND = "NOTIFICATION_APP_ENTER_BACKGROUND";

	/**
	 * Sent before payload request is sent to the server
	 */
	public static final String NOTIFICATION_PAYLOAD_WILL_SEND = "NOTIFICATION_PAYLOAD_WILL_SEND"; // { payload: Payload }

	/**
	 * Sent after payload sending if finished (might be successful or not)
	 */
	public static final String NOTIFICATION_PAYLOAD_DID_SEND = "NOTIFICATION_PAYLOAD_DID_SEND";  // { successful : boolean, payload: Payload }

	/**
	 * Sent if user requested to close all interactions.
	 */
	public static final String NOTIFICATION_INTERACTIONS_SHOULD_DISMISS = "NOTIFICATION_INTERACTIONS_SHOULD_DISMISS";

	// keys
	public static final String NOTIFICATION_KEY_SUCCESSFUL = "successful";
	public static final String NOTIFICATION_KEY_ACTIVITY = "activity";
	public static final String NOTIFICATION_KEY_CONVERSATION = "conversation";
	public static final String NOTIFICATION_KEY_PAYLOAD = "payload";
}
