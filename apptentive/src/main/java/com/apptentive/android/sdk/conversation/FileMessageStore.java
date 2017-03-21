/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.model.Payload;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.storage.MessageStore;
import com.apptentive.android.sdk.util.threading.DispatchQueue;

import java.util.List;
import java.util.concurrent.Future;

class FileMessageStore implements MessageStore {

	private final DispatchQueue operationQueue;

	public FileMessageStore(DispatchQueue operationQueue) {
		if (operationQueue == null) {
			throw new IllegalArgumentException("Operation queue is null");
		}
		this.operationQueue = operationQueue;
	}

	@Override
	public void addPayload(Payload... payloads) {

	}

	@Override
	public void deletePayload(Payload payload) {

	}

	@Override
	public void deleteAllPayloads() {

	}

	@Override
	public Future<Payload> getOldestUnsentPayload() throws Exception {
		return null;
	}

	@Override
	public void addOrUpdateMessages(ApptentiveMessage... apptentiveMessage) {

	}

	@Override
	public void updateMessage(ApptentiveMessage apptentiveMessage) {

	}

	@Override
	public Future<List<ApptentiveMessage>> getAllMessages() throws Exception {
		return null;
	}

	@Override
	public Future<String> getLastReceivedMessageId() throws Exception {
		return null;
	}

	@Override
	public Future<Integer> getUnreadMessageCount() throws Exception {
		return null;
	}

	@Override
	public void deleteAllMessages() {

	}

	@Override
	public void deleteMessage(String nonce) {

	}
}
