package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.serialization.SerializableObject;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A light weight representation of the conversation object stored on the disk.
 */
class ConversationMetadataItem implements SerializableObject {
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
	String userId;
	String filename;
	String keyId;

	public ConversationMetadataItem(DataInput in) throws IOException {
		userId = in.readUTF();
		filename = in.readUTF();
		keyId = in.readUTF();
		state = in.readByte();
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		out.writeUTF(userId);
		out.writeUTF(filename);
		out.writeUTF(keyId);
		out.writeByte(state);
	}

	public boolean isActive() {
		return state == CONVERSATION_STATE_ACTIVE;
	}

	public boolean isDefault() {
		return state == CONVERSATION_STATE_DEFAULT;
	}
}
