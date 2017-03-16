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
	final File file;

	public ConversationMetadataItem(String conversationId, File file)
	{
		if (StringUtils.isNullOrEmpty(conversationId)) {
			throw new IllegalArgumentException("Conversation id is null or empty");
		}

		if (file == null) {
			throw new IllegalArgumentException("File is null");
		}

		this.conversationId = conversationId;
		this.file = file;
	}

	public ConversationMetadataItem(DataInput in) throws IOException {
		conversationId = in.readUTF();
		file = new File(in.readUTF());
		state = ConversationState.valueOf(in.readByte());
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		out.writeUTF(conversationId);
		out.writeUTF(file.getAbsolutePath());
		out.writeByte(state.ordinal());
	}

	public String getConversationId() {
		return conversationId;
	}

	public ConversationState getState() {
		return state;
	}
}
