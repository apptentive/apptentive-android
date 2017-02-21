package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.serialization.ObjectSerialization;
import com.apptentive.android.sdk.storage.DataChangedListener;
import com.apptentive.android.sdk.storage.FileSerializer;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import java.io.File;
import java.io.IOException;

import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;

/**
 * Class responsible for managing conversations.
 * <pre>
 *   - Saving/Loading conversations from/to files.
 *   - Switching conversations when users login/logout.
 *   - Creating default conversation.
 *   - Migrating legacy conversation data.
 * </pre>
 */
public class ConversationManager implements DataChangedListener {

	protected static final String CONVERSATION_METADATA_PATH = "conversation-v1.meta";

	/**
	 * Private serial dispatch queue for background operations
	 */
	private final DispatchQueue operationQueue;

	/**
	 * A basic directory for storing conversation-related data.
	 */
	private final File storageDir;

	/**
	 * Current state of conversation metadata.
	 */
	private ConversationMetadata conversationMetadata;

	private Conversation activeConversation;

	public ConversationManager(DispatchQueue operationQueue, File storageDir) {
		if (operationQueue == null) {
			throw new IllegalArgumentException("Operation queue is null");
		}

		this.operationQueue = operationQueue;
		this.storageDir = storageDir;
	}

	/**
	 * Attempts to load an active conversation. Returns <code>false</code> if active conversation is
	 * missing or cannnot be loaded
	 */
	public boolean loadActiveConversation() {
		try {
			// resolving metadata
			conversationMetadata = resolveMetadata();

			// attempt to load existing conversation
			activeConversation = loadActiveConversationGuarded();
			if (activeConversation != null) {
				activeConversation.setDataChangedListener(this);
				return true;
			}

			// no conversation - fetch one
			fetchConversation();

		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while loading active conversation");
		}

		return false;
	}

	private void fetchConversation() {
		
	}

	private Conversation loadActiveConversationGuarded() throws IOException {
		// if the user was logged in previously - we should have an active conversation
		ApptentiveLog.v(CONVERSATION, "Loading active conversation...");
		final ConversationMetadataItem activeItem = conversationMetadata.findItem(new ConversationMetadata.Filter() {
			@Override
			public boolean accept(ConversationMetadataItem item) {
				return item.isActive();
			}
		});
		if (activeItem != null) {
			return loadConversation(activeItem);
		}

		// if no user was logged in previously - we might have a default conversation
		ApptentiveLog.v(CONVERSATION, "Loading default conversation...");
		final ConversationMetadataItem defaultItem = conversationMetadata.findItem(new ConversationMetadata.Filter() {
			@Override
			public boolean accept(ConversationMetadataItem item) {
				return item.isDefault();
			}
		});
		if (defaultItem != null) {
			return loadConversation(defaultItem);
		}

		// TODO: check for legacy conversations
		ApptentiveLog.v(CONVERSATION, "Can't load conversation");
		return null;
	}

	private Conversation loadConversation(ConversationMetadataItem item) {
		// TODO: use same serialization logic across the project
		File file = new File(item.filename);
		FileSerializer serializer = new FileSerializer(file);
		return (Conversation) serializer.deserialize();
	}

	//region Metadata

	private ConversationMetadata resolveMetadata() {
		try {
			File metaFile = new File(storageDir, CONVERSATION_METADATA_PATH);
			if (metaFile.exists()) {
				ApptentiveLog.v(CONVERSATION, "Loading meta file: " + metaFile);
				return ObjectSerialization.deserialize(metaFile, ConversationMetadata.class);
			} else {
				ApptentiveLog.v(CONVERSATION, "Meta file does not exist: " + metaFile);
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while loading conversation metadata");
		}

		return new ConversationMetadata();
	}

	//endregion

	//region DataChangedListener

	@Override
	public void onDataChanged() {
		boolean scheduled = operationQueue.dispatchAsyncOnce(saveSessionTask, 100L);
		if (scheduled) {
			ApptentiveLog.d(CONVERSATION, "Scheduling conversation save.");
		} else {
			ApptentiveLog.d(CONVERSATION, "Conversation save already scheduled.");
		}
	}

	//endregion

	//region Dispatch Tasks

	private final DispatchTask saveSessionTask = new DispatchTask() {
		@Override
		protected void execute() {
//			ApptentiveLog.d("Saving Conversation");
//			ApptentiveLog.v("EventData: %s", conversation.getEventData().toString());
//			if (fileSerializer != null) {
//				fileSerializer.serialize(conversation);
//			}

			throw new RuntimeException("Implement me");
		}
	};

	//endregion

	//region Listener
	//endregion

}
