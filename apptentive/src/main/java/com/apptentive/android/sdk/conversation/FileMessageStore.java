/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.storage.MessageStore;

import java.util.List;

class FileMessageStore implements MessageStore {
	//region MessageStore

	@Override
	public void addOrUpdateMessages(ApptentiveMessage... apptentiveMessage) {

	}

	@Override
	public void updateMessage(ApptentiveMessage apptentiveMessage) {

	}

	@Override
	public List<ApptentiveMessage> getAllMessages() throws Exception {
		return null;
	}

	@Override
	public String getLastReceivedMessageId() throws Exception {
		return null;
	}

	@Override
	public int getUnreadMessageCount() throws Exception {
		return 0;
	}

	@Override
	public void deleteAllMessages() {

	}

	@Override
	public void deleteMessage(String nonce) {

	}

	//endregion
}
