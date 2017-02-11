package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.serialization.ObjectSerialization;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import java.io.File;
import java.io.IOException;

/**
 * Class responsible for managing conversations.
 * <pre>
 *   - Saving/Loading conversations from/to files.
 *   - Switching conversations when users login/logout.
 *   - Creating default conversation.
 *   - Migrating legacy conversation data.
 * </pre>
 */
public class ConversationManager {
	/**
	 * Private serial dispatch queue for background operations
	 */
	private final DispatchQueue operationQueue;

	/**
	 * Current state of conversation metadata.
	 */
	private ConversationMetadata conversationMetadata;

	public ConversationManager(DispatchQueue operationQueue) {
		if (operationQueue == null) {
			throw new IllegalArgumentException("Operation queue is null");
		}
		this.operationQueue = operationQueue;
	}

	/**
	 * Loads current conversation asynchronously.
	 */
	public void loadCurrentConversation(Callback callback) {
		loadConversation(new Filter() {
			@Override
			public boolean accept(ConversationMetadataItem metadata) {
				return false;
			}
		}, callback);
	}

	/**
	 * Helper method for async conversation loading with specified filter.
	 */
	private void loadConversation(final Filter filter, final Callback callback) {
		operationQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				Conversation conversation = null;
				String errorMessage = null;
				try {
					conversation = loadConversationSync(filter);
				} catch (Exception e) {
					errorMessage = e.getMessage();
				}

				if (callback != null) {
					callback.onFinishLoading(conversation, errorMessage);
				}
			}
		});
	}

	/**
	 * Loads selected conversation on a background queue
	 */
	private Conversation loadConversationSync(Filter filter) throws IOException {
		if (conversationMetadata == null) {
			conversationMetadata = ObjectSerialization.deserialize(new File(""), ConversationMetadata.class);
		}

		ConversationMetadataItem matchingItem = null;
		for (ConversationMetadataItem item : conversationMetadata.getItems()) {
			if (filter.accept(item)) {
				matchingItem = item;
			}
		}

		if (matchingItem == null) {
			return null;
		}

		return null;
	}

	/**
	 * Callback listener interface
	 */
	public interface Callback {
		/**
		 * Called when conversation loading is finished.
		 *
		 * @param conversation - null if loading failed
		 * @param errorMessage - error description in case if loading failed (null is succeed)
		 */
		void onFinishLoading(Conversation conversation, String errorMessage);
	}

	/**
	 * Interface which encapsulates conversation metadata filtering (visitor pattern)
	 */
	interface Filter {
		boolean accept(ConversationMetadataItem item);
	}
}
