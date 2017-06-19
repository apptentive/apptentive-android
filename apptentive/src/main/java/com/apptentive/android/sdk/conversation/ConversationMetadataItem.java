package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.serialization.SerializableObject;
import com.apptentive.android.sdk.util.StringUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

import static com.apptentive.android.sdk.util.Util.readNullableUTF;
import static com.apptentive.android.sdk.util.Util.writeNullableUTF;

/**
 * A light weight representation of the conversation object stored on the disk.
 */
public class ConversationMetadataItem implements SerializableObject {

	/**
	 * The state of the target conversation
	 */
	ConversationState state = ConversationState.UNDEFINED;

	/**
	 * Conversation ID which was received from the backend
	 */
	final String conversationId;

	/**
	 * The token for active conversations
	 */
	String conversationToken;

	/**
	 * Storage filename for conversation serialized data
	 */
	final File dataFile;

	/**
	 * Storage filename for conversation serialized messages
	 */
	final File messagesFile;

	/**
	 * Key for encrypting payloads
	 */
	String encryptionKey;

	/**
	 * An optional user ID for logged in conversations
	 */
	String userId;

	public ConversationMetadataItem(String conversationId, File dataFile, File messagesFile) {
		if (StringUtils.isNullOrEmpty(conversationId)) {
			throw new IllegalArgumentException("Conversation id is null or empty");
		}

		if (dataFile == null) {
			throw new IllegalArgumentException("Data file is null");
		}

		if (messagesFile == null) {
			throw new IllegalArgumentException("Messages file is null");
		}

		this.conversationId = conversationId;
		this.dataFile = dataFile;
		this.messagesFile = messagesFile;
	}

	public ConversationMetadataItem(DataInput in) throws IOException {
		conversationId = in.readUTF();
		conversationToken = readNullableUTF(in);
		dataFile = new File(in.readUTF());
		messagesFile = new File(in.readUTF());
		state = ConversationState.valueOf(in.readByte());
		encryptionKey = readNullableUTF(in);
		userId = readNullableUTF(in);
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		out.writeUTF(conversationId);
		writeNullableUTF(out, conversationToken);
		out.writeUTF(dataFile.getAbsolutePath());
		out.writeUTF(messagesFile.getAbsolutePath());
		out.writeByte(state.ordinal());
		writeNullableUTF(out, encryptionKey);
		writeNullableUTF(out, userId);
	}

	public String getConversationId() {
		return conversationId;
	}

	public ConversationState getState() {
		return state;
	}

	public String getEncryptionKey() {
		return encryptionKey;
	}

	public String getUserId() {
		return userId;
	}

	public String getConversationToken() {
		return conversationToken;
	}

	@Override
	public String toString() {
		return "ConversationMetadataItem{" +
			       "state=" + state +
			       ", conversationId='" + conversationId + '\'' +
			       ", conversationToken='" + conversationToken + '\'' +
			       ", dataFile=" + dataFile +
			       ", messagesFile=" + messagesFile +
			       ", encryptionKey='" + encryptionKey + '\'' +
			       ", userId='" + userId + '\'' +
			       '}';
	}
}
