package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.serialization.SerializableObject;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class which represents all conversation entries stored on the disk
 */
class ConversationMetadata implements SerializableObject {
	private static final byte VERSION = 1;

	private final List<ConversationMetadataItem> items;

	public ConversationMetadata() {
		items = new ArrayList<>();
	}

	//region Serialization

	public ConversationMetadata(DataInput in) throws IOException {
		byte version = in.readByte();
		if (version != VERSION) {
			throw new IOException("Expected version " + VERSION + " but was " + version);
		}

		int count = in.readByte();
		items = new ArrayList<>(count);
		for (int i = 0; i < count; ++i) {
			items.add(new ConversationMetadataItem(in));
		}
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		out.writeByte(VERSION);
		out.write(items.size());
		for (int i = 0; i < items.size(); ++i) {
			items.get(i).writeExternal(out);
		}
	}

	//endregion

	//region Getters/Setters

	public List<ConversationMetadataItem> getItems() {
		return items;
	}

	//endregion
}
