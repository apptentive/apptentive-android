/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.debug.Assert;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageFactory;
import com.apptentive.android.sdk.serialization.SerializableObject;
import com.apptentive.android.sdk.storage.MessageStore;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class FileMessageStore implements MessageStore {
	/**
	 * Binary format version
	 */
	private static final byte VERSION = 1;

	private File file;
	private final List<MessageEntry> messageEntries;
	private boolean shouldFetchFromFile;

	FileMessageStore(File file) {
		this();
		this.file = file;
		this.shouldFetchFromFile = true; // we would lazily read it from a file later
	}

	FileMessageStore() {
		this.messageEntries = new ArrayList<>(); // we need a random access
	}

	//region MessageStore

	@Override
	public synchronized void addOrUpdateMessages(ApptentiveMessage... apptentiveMessages) {
		fetchEntries();

		for (ApptentiveMessage apptentiveMessage : apptentiveMessages) {
			MessageEntry existing = findMessageEntry(apptentiveMessage);
			if (existing != null) {
				// Update
				existing.id = apptentiveMessage.getId();
				existing.state = apptentiveMessage.getState().name();
				if (apptentiveMessage.isRead()) { // A apptentiveMessage can't be unread after being read.
					existing.isRead = true;
				}
				existing.json = apptentiveMessage.toString();
			} else {
				// Insert
				MessageEntry entry = new MessageEntry();
				entry.id = apptentiveMessage.getId();
				entry.clientCreatedAt = apptentiveMessage.getClientCreatedAt();
				entry.nonce = apptentiveMessage.getNonce();
				entry.state = apptentiveMessage.getState().name();
				entry.isRead = apptentiveMessage.isRead();
				entry.json = apptentiveMessage.toString();
				messageEntries.add(entry);
			}
		}

		writeToFile();
	}

	@Override
	public synchronized void updateMessage(ApptentiveMessage apptentiveMessage) {
		fetchEntries();

		MessageEntry entry = findMessageEntry(apptentiveMessage);
		if (entry != null) {
			entry.id = apptentiveMessage.getId();
			entry.clientCreatedAt = apptentiveMessage.getClientCreatedAt();
			entry.nonce = apptentiveMessage.getNonce();
			entry.state = apptentiveMessage.getState().name();
			if (apptentiveMessage.isRead()) { // A apptentiveMessage can't be unread after being read.
				entry.isRead = true;
			}
			entry.json = apptentiveMessage.toString();
			writeToFile();
		}
	}

	@Override
	public synchronized List<ApptentiveMessage> getAllMessages() throws Exception {
		fetchEntries();

		List<ApptentiveMessage> apptentiveMessages = new ArrayList<>();
		for (MessageEntry entry : messageEntries) {
			ApptentiveMessage apptentiveMessage = MessageFactory.fromJson(entry.json);
			if (apptentiveMessage == null) {
				ApptentiveLog.e("Error parsing Record json from database: %s", entry.json);
				continue;
			}
			apptentiveMessage.setState(ApptentiveMessage.State.parse(entry.state));
			apptentiveMessage.setRead(entry.isRead);
			apptentiveMessages.add(apptentiveMessage);
		}
		return apptentiveMessages;
	}

	@Override
	public synchronized String getLastReceivedMessageId() throws Exception {
		fetchEntries();

		final String savedState = ApptentiveMessage.State.saved.name();
		for (int i = messageEntries.size() - 1; i >= 0; --i) {
			final MessageEntry entry = messageEntries.get(i);
			if (StringUtils.equal(entry.state, savedState) && entry.id != null) {
				return entry.id;
			}
		}
		return null;
	}

	@Override
	public synchronized int getUnreadMessageCount() throws Exception {
		fetchEntries();

		int count = 0;
		for (MessageEntry entry : messageEntries) {
			if (!entry.isRead && entry.id != null) {
				++count;
			}
		}
		return count;
	}

	@Override
	public synchronized void deleteAllMessages() {
		messageEntries.clear();
		writeToFile();
	}

	@Override
	public synchronized void deleteMessage(String nonce) {
		fetchEntries();

		for (int i = 0; i < messageEntries.size(); ++i) {
			if (StringUtils.equal(nonce, messageEntries.get(i).nonce)) {
				messageEntries.remove(i);
				writeToFile();
				break;
			}
		}
	}

	//endregion

	//region File save/load

	private synchronized void fetchEntries() {
		if (shouldFetchFromFile) {
			readFromFile();
			shouldFetchFromFile = false;
		}
	}

	private synchronized void readFromFile() {
		messageEntries.clear();
		try {
			if (file.exists()) {
				List<MessageEntry> entries = readFromFileGuarded();
				messageEntries.addAll(entries);
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while reading entries");
		}
	}

	private List<MessageEntry> readFromFileGuarded() throws IOException {
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(new FileInputStream(file));
			byte version = dis.readByte();
			if (version != VERSION) {
				throw new IOException("Unsupported binary version: " + version);
			}
			int entryCount = dis.readInt();
			List<MessageEntry> entries = new ArrayList<>();
			for (int i = 0; i < entryCount; ++i) {
				entries.add(new MessageEntry(dis));
			}
			return entries;
		} finally {
			Util.ensureClosed(dis);
		}
	}

	private synchronized void writeToFile() {
		Assert.assertFalse(file != null, "File is not specified");
		if (file == null) {
			return;
		}

		try {
			writeToFileGuarded();
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while saving messages");
		}
		shouldFetchFromFile = false; // mark it as not shouldFetchFromFile to keep a memory version
	}

	private void writeToFileGuarded() throws IOException {
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(new FileOutputStream(file));
			dos.writeByte(VERSION);
			dos.writeInt(messageEntries.size());
			for (MessageEntry entry : messageEntries) {
				entry.writeExternal(dos);
			}
		} finally {
			Util.ensureClosed(dos);
		}
	}

	//endregion

	//region Filtering

	private MessageEntry findMessageEntry(ApptentiveMessage message) {
		Assert.assertNotNull(message);
		return message != null ? findMessageEntry(message.getNonce()) : null;
	}

	private MessageEntry findMessageEntry(String nonce) {
		for (MessageEntry entry : messageEntries) {
			if (StringUtils.equal(entry.nonce, nonce)) {
				return entry;
			}
		}
		return null;
	}

	//endregion

	//region Properties

	public synchronized void setFile(File file) {
		if (file == null) {
			throw new IllegalArgumentException("File is null");
		}

		this.file = file;
		shouldFetchFromFile = true;
	}


	//endregion

	//region Message Entry

	private static class MessageEntry implements SerializableObject {
		String id;
		double clientCreatedAt;
		String nonce;
		String state;
		boolean isRead;
		String json;

		MessageEntry() {
		}

		MessageEntry(DataInput in) throws IOException {
			id = readNullableUTF(in);
			clientCreatedAt = in.readDouble();
			nonce = readNullableUTF(in);
			state = readNullableUTF(in);
			isRead = in.readBoolean();
			json = readNullableUTF(in);
		}

		@Override
		public void writeExternal(DataOutput out) throws IOException {
			writeNullableUTF(out, id);
			out.writeDouble(clientCreatedAt);
			writeNullableUTF(out, nonce);
			writeNullableUTF(out, state);
			out.writeBoolean(isRead);
			writeNullableUTF(out, json);
		}

		private static void writeNullableUTF(DataOutput out, String value) throws IOException {
			out.writeBoolean(value != null);
			if (value != null) {
				out.writeUTF(value);
			}
		}

		private static String readNullableUTF(DataInput in) throws IOException {
			boolean notNull = in.readBoolean();
			return notNull ? in.readUTF() : null;
		}
	}

	//endregion
}
