/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

public class ApptentiveNotifications {

	/**
	 * Sent when conversation loading is finished
	 */
	public static final String NOTIFICATION_CONVERSATION_LOAD_DID_FINISH = "CONVERSATION_LOAD_DID_FINISH"; // { conversation : Conversation, successful: Boolean }

	/**
	 * Sent when conversation data changes
	 */
	public static final String NOTIFICATION_CONVERSATION_DATA_DID_CHANGE = "CONVERSATION_DATA_DID_CHANGE"; // { conversation : Conversation }

	/**
	 * Sent when conversation state changes (user logs out, etc)
	 */
	public static final String NOTIFICATION_CONVERSATION_STATE_DID_CHANGE = "CONVERSATION_STATE_DID_CHANGE"; // { conversation : Conversation }

	/**
	 * Sent when conversation token starts fetching
	 */
	public static final String NOTIFICATION_CONVERSATION_TOKEN_WILL_FETCH = "CONVERSATION_TOKEN_WILL_FETCH"; // { conversation : Conversation }

	/**
	 * Sent when conversation token fetch completes
	 */
	public static final String NOTIFICATION_CONVERSATION_TOKEN_DID_FETCH = "CONVERSATION_TOKEN_DID_FETCH"; // { conversation : Conversation, successful: Boolean }

	/**
	 * Sent when conversation is about to be logged out, to allow necessary tasks to be completed within the ending conversation.
	 */
	public static final String NOTIFICATION_CONVERSATION_WILL_LOGOUT = "CONVERSATION_WILL_LOGOUT"; // { conversation : Conversation }

	/**
	 * Sent when message polling is started
	 */
	public static final String NOTIFICATION_MESSAGES_STARTED_POLLING = "MESSAGES_STARTED_POLLING"; // { interval: Long }

	/**
	 * Sent when message polling is stopped
	 */
	public static final String NOTIFICATION_MESSAGES_STOPPED_POLLING = "MESSAGES_STOPPED_POLLING";

	/**
	 * Sent if a new activity is started.
	 */
	public static final String NOTIFICATION_ACTIVITY_STARTED = "ACTIVITY_STARTED"; // { activity : Activity }

	/**
	 * Sent if a new activity is stopped.
	 */
	public static final String NOTIFICATION_ACTIVITY_STOPPED = "ACTIVITY_STOPPED"; // { activity : Activity }

	/**
	 * Sent if activity is resumed.
	 */
	public static final String NOTIFICATION_ACTIVITY_RESUMED = "ACTIVITY_RESUMED"; // { activity : Activity }

	/**
	 * Sent if app entered foreground
	 */
	public static final String NOTIFICATION_APP_ENTERED_FOREGROUND = "APP_ENTERED_FOREGROUND";

	/**
	 * Sent if app entered background
	 */
	public static final String NOTIFICATION_APP_ENTERED_BACKGROUND = "APP_ENTERED_BACKGROUND";

	/**
	 * Event is generated
	 */
	public static final String NOTIFICATION_EVENT_GENERATED = "EVENT_GENERATED"; // { event: EventPayload }

	/**
	 * Sent before payload request is sent to the server
	 */
	public static final String NOTIFICATION_PAYLOAD_WILL_START_SEND = "PAYLOAD_WILL_START_SEND"; // { payload: PayloadData }

	/**
	 * Sent after payload sending if finished (might be successful or not)
	 */
	public static final String NOTIFICATION_PAYLOAD_DID_FINISH_SEND = "PAYLOAD_DID_FINISH_SEND";  // { successful : boolean, payload: PayloadData, responseCode: int, responseData: JSONObject }

	/**
	 * Sent if user requested to close all interactions.
	 */
	public static final String NOTIFICATION_INTERACTIONS_SHOULD_DISMISS = "INTERACTIONS_SHOULD_DISMISS";

	/**
	 * Sent when a request to the server fails with a 401, and external code needs to be notified.
	 */
	public static final String NOTIFICATION_AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED"; // { conversationId : String, authenticationFailedReason: AuthenticationFailedReason }

	/**
	 * Sent when interactions are fetched for any conversation. Used right now so espresso tests know when they can run.
	 */
	public static final String NOTIFICATION_INTERACTIONS_DID_FETCH = "INTERACTIONS_DID_FETCH"; // { successful: Boolean }

	/**
	 * Sent when interaction manifest data is fetched for any conversation. Used by the log monitor.
	 */
	public static final String NOTIFICATION_INTERACTION_MANIFEST_FETCHED = "INTERACTION_MANIFEST_FETCHED"; // { manifest: String }

	/**
	 * Sent when message store changes.
	 */
	public static final String NOTIFICATION_MESSAGE_STORE_DID_CHANGE = "MESSAGE_STORE_DID_CHANGE"; // { messageStore: MessageStore }

	/**
	 * Sent when advertiser id was resolved.
	 */
	public static final String NOTIFICATION_ADVERTISER_ID_DID_RESOLVE = "ADVERTISER_ID_DID_RESOLVE"; // { successful: Boolean, clientInfo: AdvertisingIdClientInfo }

	/**
	 * Sent when advertiser id was resolved.
	 */
	public static final String NOTIFICATION_CONFIGURATION_FETCH_DID_FINISH = "CONFIGURATION_FETCH_DID_FINISH"; // { configuration: Configuration, conversation: Conversation }

	/**
	 * Sent when log monitor starts capturing and storing logs.
	 */
	public static final String NOTIFICATION_LOG_MONITOR_STARTED = "LOG_MONITOR_STARTED";

	/**
	 * Sent when log monitor stops capturing and storing logs.
	 */
	public static final String NOTIFICATION_LOG_MONITOR_STOPPED = "LOG_MONITOR_STOPPED";

	// keys
	public static final String NOTIFICATION_KEY_SUCCESSFUL = "successful";
	public static final String NOTIFICATION_KEY_ACTIVITY = "activity";
	public static final String NOTIFICATION_KEY_CONVERSATION = "conversation";
	public static final String NOTIFICATION_KEY_CONVERSATION_ID = "conversationId";
	public static final String NOTIFICATION_KEY_EVENT = "event";
	public static final String NOTIFICATION_KEY_AUTHENTICATION_FAILED_REASON = "authenticationFailedReason";// type: AuthenticationFailedReason
	public static final String NOTIFICATION_KEY_PAYLOAD = "payload";
	public static final String NOTIFICATION_KEY_CONFIGURATION = "configuration";
	public static final String NOTIFICATION_KEY_RESPONSE_CODE = "responseCode";
	public static final String NOTIFICATION_KEY_RESPONSE_DATA = "responseData";
	public static final String NOTIFICATION_KEY_MANIFEST = "manifest";
	public static final String NOTIFICATION_KEY_MESSAGE_STORE = "messageStore";
	public static final String NOTIFICATION_KEY_INTERVAL = "interval";
	public static final String NOTIFICATION_KEY_ADVERTISER_CLIENT_INFO = "clientInfo";
}
