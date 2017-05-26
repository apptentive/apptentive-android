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
	 * Sent if app entered foreground
	 */
	public static final String NOTIFICATION_APP_ENTERED_FOREGROUND = "NOTIFICATION_APP_ENTERED_FOREGROUND";

	/**
	 * Sent if app entered background
	 */
	public static final String NOTIFICATION_APP_ENTERED_BACKGROUND = "NOTIFICATION_APP_ENTERED_BACKGROUND";

	/**
	 * Sent before payload request is sent to the server
	 */
	public static final String NOTIFICATION_PAYLOAD_WILL_START_SEND = "NOTIFICATION_PAYLOAD_WILL_START_SEND"; // { payload: PayloadData }

	/**
	 * Sent after payload sending if finished (might be successful or not)
	 */
	public static final String NOTIFICATION_PAYLOAD_DID_FINISH_SEND = "NOTIFICATION_PAYLOAD_DID_FINISH_SEND";  // { successful : boolean, payload: PayloadData, responseCode: int, responseData: JSONObject }

	/**
	 * Sent if user requested to close all interactions.
	 */
	public static final String NOTIFICATION_INTERACTIONS_SHOULD_DISMISS = "NOTIFICATION_INTERACTIONS_SHOULD_DISMISS";

	// keys
	public static final String NOTIFICATION_KEY_SUCCESSFUL = "successful";
	public static final String NOTIFICATION_KEY_ACTIVITY = "activity";
	public static final String NOTIFICATION_KEY_CONVERSATION = "conversation";
	public static final String NOTIFICATION_KEY_PAYLOAD = "payload";
	public static final String NOTIFICATION_KEY_RESPONSE_CODE = "responseCode";
	public static final String NOTIFICATION_KEY_RESPONSE_DATA = "responseData";
}
