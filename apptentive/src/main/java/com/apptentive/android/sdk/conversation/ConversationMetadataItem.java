package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.serialization.SerializableObject;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A light weight representation of the conversation object stored on the disk.
 */
class ConversationMetadataItem implements SerializableObject {
	String userId;
	String filename;
	String key;
	boolean active;

	public ConversationMetadataItem(DataInput in) throws IOException {
		userId = in.readUTF();
		filename = in.readUTF();
		key = in.readUTF();
		active = in.readBoolean();
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		out.writeUTF(userId);
		out.writeUTF(filename);
		out.writeUTF(key);
		out.writeBoolean(active);
	}
}
