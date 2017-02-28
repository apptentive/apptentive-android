package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.serialization.SerializableObject;
import com.apptentive.android.sdk.util.StringUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.apptentive.android.sdk.conversation.ConversationMetadataItem.*;

/**
 * Class which represents all conversation entries stored on the disk
 */
public class ConversationMetadata implements SerializableObject, Iterable<ConversationMetadataItem> {
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

	//region Conversation

	// TODO: replace it with notifications so that the active conversation can send out events and clean itself up.
	void setActiveConversation(final Conversation conversation) {
		if (conversation != null) {
			// clear 'active' state
			boolean found = false;
			for (ConversationMetadataItem item : items) {
				if (StringUtils.equal(conversation.getConversationId(), item.conversationId)) {
					found = true;
					item.state = CONVERSATION_STATE_ACTIVE;
				} else if (item.state == CONVERSATION_STATE_ACTIVE) {
					item.state = CONVERSATION_STATE_INACTIVE;
				}
			}

			// add a new item if it was not found
			if (!found) {
				final ConversationMetadataItem item = new ConversationMetadataItem(conversation.getConversationId(), conversation.getFilename());
				item.state = CONVERSATION_STATE_ACTIVE;
				items.add(item);
			}
		} else {
			for (ConversationMetadataItem item : items) {
				if (item.state == CONVERSATION_STATE_ACTIVE) {
					item.state = CONVERSATION_STATE_ACTIVE;
				}
			}
		}
	}

	//endregion

	//region Filtering

	public ConversationMetadataItem findItem(Filter filter) {
		for (ConversationMetadataItem item : items) {
			if (filter.accept(item)) {
				return item;
			}
		}
		return null;
	}

	//endregion

	//region Iterable

	@Override
	public Iterator<ConversationMetadataItem> iterator() {
		return items.iterator();
	}

	//endregion

	//region Getters/Setters

	public List<ConversationMetadataItem> getItems() {
		return items;
	}

	//endregion

	//region Filter

	public interface Filter {
		boolean accept(ConversationMetadataItem item);
	}

	//endregion
}
