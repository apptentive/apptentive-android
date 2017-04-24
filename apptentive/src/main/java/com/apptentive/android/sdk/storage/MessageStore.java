/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;


import com.apptentive.android.sdk.model.ApptentiveMessage;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public interface MessageStore {

	void addOrUpdateMessages(ApptentiveMessage... apptentiveMessage);

	void updateMessage(ApptentiveMessage apptentiveMessage);

	List<ApptentiveMessage> getAllMessages() throws Exception;

	String getLastReceivedMessageId() throws Exception;

	int getUnreadMessageCount() throws Exception;

	void deleteAllMessages();

	void deleteMessage(String nonce);

	ApptentiveMessage findMessage(String nonce);
}
