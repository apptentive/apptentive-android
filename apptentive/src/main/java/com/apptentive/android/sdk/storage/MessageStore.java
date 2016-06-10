/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;


import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Sky Kelsey
 */
public interface MessageStore extends PayloadStore {

	void addOrUpdateMessages(ApptentiveMessage... apptentiveMessage);

	void updateMessage(ApptentiveMessage apptentiveMessage);

	Future<List<ApptentiveMessage>> getAllMessages() throws Exception;

	Future<String> getLastReceivedMessageId() throws Exception;

	Future<Integer> getUnreadMessageCount() throws Exception;

	void deleteAllMessages();

	void deleteMessage(String nonce);
}
