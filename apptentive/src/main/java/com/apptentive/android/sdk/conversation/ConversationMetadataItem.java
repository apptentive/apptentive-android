package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.serialization.SerializableObject;
import com.apptentive.android.sdk.util.StringUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A light weight representation of the conversation object stored on the disk.
 */
public class ConversationMetadataItem implements SerializableObject {
	/**
	 * Conversation state is not known
	 */
	public static byte CONVERSATION_STATE_UNDEFINED = 0;

	/**
	 * No users have logged-in yet (guest mode)
	 */
	public static byte CONVERSATION_STATE_DEFAULT = 1;

	/**
	 * The conversation belongs to the currently logged-in user.
	 */
	public static byte CONVERSATION_STATE_ACTIVE = 2;

	/**
	 * The conversation belongs to a logged-out user.
	 */
	public static byte CONVERSATION_STATE_INACTIVE = 3;

	byte state = CONVERSATION_STATE_UNDEFINED;
	String conversationId;
	String filename;

	public ConversationMetadataItem(String conversationId, String filename)
	{
		if (StringUtils.isNullOrEmpty(conversationId)) {
			throw new IllegalArgumentException("Conversation id is null or empty");
		}

		if (StringUtils.isNullOrEmpty(filename)) {
			throw new IllegalArgumentException("Filename is null or empty");
		}

		this.conversationId = conversationId;
		this.filename = filename;
	}

	public ConversationMetadataItem(DataInput in) throws IOException {
		conversationId = in.readUTF();
		filename = in.readUTF();
		state = in.readByte();
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		out.writeUTF(conversationId);
		out.writeUTF(filename);
		out.writeByte(state);
	}

	public String getConversationId() {
		return conversationId;
	}

	public boolean isActive() {
		return state == CONVERSATION_STATE_ACTIVE;
	}

	public boolean isDefault() {
		return state == CONVERSATION_STATE_DEFAULT;
	}
}
