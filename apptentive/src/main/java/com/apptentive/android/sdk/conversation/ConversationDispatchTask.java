/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import androidx.annotation.Nullable;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInstance;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.debug.ErrorMetrics;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;
import static com.apptentive.android.sdk.util.StringUtils.isNullOrEmpty;

public abstract class ConversationDispatchTask extends DispatchTask {
	private final Apptentive.BooleanCallback callback;
	private final DispatchQueue callbackQueue;
	private String description = "dispatch task";

	public ConversationDispatchTask() {
		this(null, null);
	}

	public ConversationDispatchTask(@Nullable Apptentive.BooleanCallback callback) {
		this(callback, null);
	}

	public ConversationDispatchTask(@Nullable Apptentive.BooleanCallback callback, @Nullable DispatchQueue callbackQueue) {
		this.callback = callback;
		this.callbackQueue = callbackQueue;
	}

	@Override
	protected void execute() {
		try {
			executeGuarded();
		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, e, "Exception while trying to %s", description);
			logException(e);

			notifyFailure(e);
		}
	}

	private void executeGuarded() {
		ApptentiveInstance sharedInstance = ApptentiveInternal.getInstance();
		if (sharedInstance.isNull()) {
			ApptentiveLog.e(CONVERSATION, "Unable to %s: Apptentive SDK is not initialized.", description);
			notifyFailure(null);
			return;
		}

		Conversation conversation = sharedInstance.getConversation();
		if (conversation == null) {
			ApptentiveLog.e(CONVERSATION, "Unable to %s: no active conversation.", description);
			notifyFailure(null);
			return;
		}

		boolean result = execute(conversation);
		invokeCallback(result);
	}

	protected abstract boolean execute(Conversation conversation);

	private void invokeCallback(final boolean result) {
		if (callback != null) {
			try {
				if (callbackQueue != null) {
					callbackQueue.dispatchAsync(new DispatchTask() {
						@Override
						protected void execute() {
							callback.onFinish(result);
						}
					});
				} else {
					callback.onFinish(result);
				}
			} catch (Exception e) {
				ApptentiveLog.e(CONVERSATION, "Exception while invoking callback");
				logException(e);
			}
		}
	}

	private void notifyFailure(Throwable error) {
		try {
			if (error != null) {
				onExecuteError(error);
			} else {
				onExecuteFail();
			}
		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, "Exception while handling task failure");
			logException(e);
		}
	}

	protected void onExecuteError(Throwable error) {
		onExecuteFail();
		MetricModule.sendError(error, null, null);
	}

	protected void onExecuteFail() {
		invokeCallback(false);
	}

	public ConversationDispatchTask setDescription(String description) {
		if (isNullOrEmpty(description)) {
			throw new IllegalArgumentException("Description is null or empty");
		}
		this.description = description;
		return this;
	}

	private void logException(Exception e) {
		ErrorMetrics.logException(e); // TODO: add more context info
	}
}