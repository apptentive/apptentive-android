/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import com.apptentive.android.sdk.conversation.ConversationDispatchTask;
import com.apptentive.android.sdk.debug.Assert;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchQueueType;
import com.apptentive.android.sdk.util.threading.DispatchTask;

/**
 * Helper class for dispatching SDK-related operations. Properly handles missing SDK initialization
 * and non-active conversations.
 */
public final class ApptentiveHelper {
	public static void dispatchConversationTask(ConversationDispatchTask task, String description) {
		dispatchOnConversationQueue(task.setDescription(description));
	}

	public static void dispatchOnConversationQueue(DispatchTask task) {
		conversationQueue().dispatchAsync(task);
	}

	public static boolean dispatchOnConversationQueueOnce(DispatchTask task, long delayMillis) {
		return conversationQueue().dispatchAsyncOnce(task, delayMillis);
	}

	public static DispatchQueue conversationQueue() {
		return Holder.CONVERSATION_QUEUE;
	}

	public static DispatchQueue conversationDataQueue() {
		return Holder.CONVERSATION_DATA_QUEUE;
	}

	public static boolean isConversationQueue() {
		return conversationQueue().isCurrent();
	}

	public static void checkConversationQueue() {
		Assert.assertDispatchQueue(conversationQueue());
	}

	// Thread-safe singleton implementation
	private static class Holder {
		static DispatchQueue CONVERSATION_QUEUE = createConversationQueue();
		static DispatchQueue CONVERSATION_DATA_QUEUE = createConversationDataQueue();

		private static DispatchQueue createConversationQueue() {
			try {
				return DispatchQueue.createBackgroundQueue("Apptentive Queue", DispatchQueueType.Serial);
			} catch (Exception e) {
				return null; // let unit test handle this
			}
		}

		private static DispatchQueue createConversationDataQueue() {
			try {
				return DispatchQueue.createBackgroundQueue("Apptentive Conversation Data Queue", DispatchQueueType.Serial);
			} catch (Exception e) {
				return null; // let unit test handle this
			}
		}
	}
}
