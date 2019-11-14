/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import androidx.annotation.NonNull;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.debug.Assert;
import com.apptentive.android.sdk.encryption.EncryptionException;
import com.apptentive.android.sdk.debug.ErrorMetrics;

import com.apptentive.android.sdk.encryption.EncryptionHelper;
import com.apptentive.android.sdk.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageFactory;
import com.apptentive.android.sdk.serialization.SerializableObject;
import com.apptentive.android.sdk.storage.MessageStore;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;
import static com.apptentive.android.sdk.ApptentiveLogTag.MESSAGES;
import static com.apptentive.android.sdk.util.Util.readNullableBoolean;
import static com.apptentive.android.sdk.util.Util.readNullableDouble;
import static com.apptentive.android.sdk.util.Util.readNullableUTF;
import static com.apptentive.android.sdk.util.Util.writeNullableBoolean;
import static com.apptentive.android.sdk.util.Util.writeNullableDouble;
import static com.apptentive.android.sdk.util.Util.writeNullableUTF;

class FileMessageStore implements MessageStore {
	/**
	 * Binary format version
	 */
	private static final byte VERSION = 1;

	private final File file;
	private final List<MessageEntry> messageEntries;
	private Encryption encryption;
	private boolean shouldFetchFromFile;

	FileMessageStore(File file, Encryption encryption) {
		if (file == null) {
			throw new IllegalArgumentException("File is null");
		}

		if (encryption == null) {
			throw new IllegalArgumentException("Encryption key is null");
		}

		this.file = file;
		this.encryption = encryption;
		this.messageEntries = new ArrayList<>(); // we need a random access
		this.shouldFetchFromFile = true; // we would lazily read it from a file later
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
				if (apptentiveMessage.isRead()) { // A message can't be unread after being read.
					existing.isRead = true;
				}
				existing.json = apptentiveMessage.getJsonObject().toString();
			} else {
				// Insert
				MessageEntry entry = new MessageEntry();
				entry.id = apptentiveMessage.getId();
				entry.clientCreatedAt = apptentiveMessage.getClientCreatedAt();
				entry.nonce = apptentiveMessage.getNonce();
				entry.state = apptentiveMessage.getState().name();
				entry.isRead = apptentiveMessage.isRead();
				entry.json = apptentiveMessage.getJsonObject().toString();
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
			entry.json = apptentiveMessage.getJsonObject().toString();
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
				ApptentiveLog.e(MESSAGES, "Error parsing Record json from database: %s", entry.json);
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
	public synchronized int getUnreadMessageCount() {
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

	@Override
	public ApptentiveMessage findMessage(String nonce) {
		fetchEntries();

		for (int i = 0; i < messageEntries.size(); ++i) {
			final MessageEntry messageEntry = messageEntries.get(i);
			if (StringUtils.equal(nonce, messageEntry.nonce)) {
				return MessageFactory.fromJson(messageEntry.json);
			}
		}

		return null;
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
			ApptentiveLog.e(MESSAGES, e, "Exception while reading entries");
			logException(e);
		}
	}

	private List<MessageEntry> readFromFileGuarded() throws IOException, EncryptionException {
		byte[] bytes = EncryptionHelper.readFromEncryptedFile(encryption, file);
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

		DataInputStream dis = new DataInputStream(bis);
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
	}

	private synchronized void writeToFile() {
		try {
			writeToFileGuarded();
		} catch (Exception e) {
			ApptentiveLog.e(MESSAGES, e, "Exception while saving messages");
			logException(e);
		}
		shouldFetchFromFile = false; // mark it as not shouldFetchFromFile to keep a memory version
	}

	private void writeToFileGuarded() throws IOException, EncryptionException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeByte(VERSION);
		dos.writeInt(messageEntries.size());
		for (MessageEntry entry : messageEntries) {
			entry.writeExternal(dos);
		}
		long start = System.currentTimeMillis();
		EncryptionHelper.writeToEncryptedFile(encryption, file, bos.toByteArray());
		ApptentiveLog.v(MESSAGES, "Messages saved. Took %d ms", System.currentTimeMillis() - start);
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

	void updateEncryption(@NonNull Encryption encryption) {
		if (encryption == null) {
			throw new IllegalArgumentException("Encryption is null");
		}
		this.encryption = encryption;

		// update storage
		writeToFile();
	}

	//endregion

	//region Message Entry

	private static class MessageEntry implements SerializableObject {
		String id;
		Double clientCreatedAt;
		String nonce;
		String state;
		Boolean isRead;
		String json;

		MessageEntry() {
		}

		MessageEntry(DataInput in) throws IOException {
			id = readNullableUTF(in);
			clientCreatedAt = readNullableDouble(in);
			nonce = readNullableUTF(in);
			state = readNullableUTF(in);
			isRead = readNullableBoolean(in);
			json = readNullableUTF(in);
		}

		@Override
		public void writeExternal(DataOutput out) throws IOException {
			writeNullableUTF(out, id);
			writeNullableDouble(out, clientCreatedAt);
			writeNullableUTF(out, nonce);
			writeNullableUTF(out, state);
			writeNullableBoolean(out, isRead);
			writeNullableUTF(out, json);
		}

		@Override
		public String toString() {
			return "MessageEntry{" +
				       "id='" + id + '\'' +
				       ", clientCreatedAt=" + clientCreatedAt +
				       ", nonce='" + nonce + '\'' +
				       ", state='" + state + '\'' +
				       ", isRead=" + isRead +
				       ", json='" + json + '\'' +
				       '}';
		}
	}

	//endregion

	//region Migration

	public void migrateLegacyStorage() {
		try {
			File unencryptedFile = Util.getUnencryptedFilename(file);
			if (unencryptedFile.exists()) {
				try {
					List<MessageEntry> entries = readFromLegacyFile(unencryptedFile);
					messageEntries.addAll(entries);
					writeToFile();
				} finally {
					boolean deleted = unencryptedFile.delete();
					ApptentiveLog.d(CONVERSATION, "Deleted legacy message storage: %b", deleted);
				}
			}
		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, e, "Exception while migrating messages");
			logException(e);
		}
	}

	private static List<MessageEntry> readFromLegacyFile(File file) throws IOException {
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

	//endregion

	//region Error Reporting

	private void logException(Exception e) {
		ErrorMetrics.logException(e);
	}

	//endregion

	@Override
	public String toString() {
		return "FileMessageStore{" +
			       "file=" + file +
			       ", messageEntries=" + messageEntries +
			       ", shouldFetchFromFile=" + shouldFetchFromFile +
			       '}';
	}
}
