/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

public enum ConversationState {
	/**
	 * Conversation state is not known
	 */
	UNDEFINED,

	/**
	 * A Legacy Conversation has been migrated, but still has an old OAuth token. Waiting to get a new
	 * JWT and Conversation ID from the server.
	 */
	LEGACY_PENDING,

	/**
	 * No logged in user and no conversation token
	 */
	ANONYMOUS_PENDING,

	/**
	 * No logged in user with conversation token
	 */
	ANONYMOUS,

	/**
	 * The conversation belongs to the currently logged-in user
	 */
	LOGGED_IN,

	/**
	 * The conversation belongs to a logged-out user
	 */
	LOGGED_OUT;

	/**
	 * Returns the <code>{@link ConversationState}</code> object corresponding
	 * to <code>value</code> or <code>UNDEFINED</code> if <code>value</code>
	 * is out of range
	 */
	public static ConversationState valueOf(byte value) {
		final ConversationState[] values = ConversationState.values();

		if (value >= 0 && value < values.length) {
			return values[value];
		}

		return UNDEFINED;
	}
}
