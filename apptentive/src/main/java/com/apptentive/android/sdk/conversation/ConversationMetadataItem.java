package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.serialization.SerializableObject;
import com.apptentive.android.sdk.util.StringUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

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
	 * Storage filename for conversation serialized data
	 */
	final File dataFile;

	/**
	 * Storage filename for conversation serialized messages
	 */
	final File messagesFile;

	public ConversationMetadataItem(String conversationId, File dataFile, File messagesFile)
	{
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
		dataFile = new File(in.readUTF());
		messagesFile = new File(in.readUTF());
		state = ConversationState.valueOf(in.readByte());
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		out.writeUTF(conversationId);
		out.writeUTF(dataFile.getAbsolutePath());
		out.writeUTF(messagesFile.getAbsolutePath());
		out.writeByte(state.ordinal());
	}

	public String getConversationId() {
		return conversationId;
	}

	public ConversationState getState() {
		return state;
	}
}
