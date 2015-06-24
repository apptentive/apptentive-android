/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

	import android.content.Context;
	import com.apptentive.android.sdk.model.Event;
	import com.apptentive.android.sdk.module.messagecenter.model.Message;
	import com.apptentive.android.sdk.module.messagecenter.MessageManager;
	import com.apptentive.android.sdk.module.metric.MetricModule;

	import java.util.HashMap;
	import java.util.Map;

/**
 * @author Sky Kelsey
 */
public abstract class MessageView<T extends Message> extends MessageCenterListItemView {

	public MessageView(final Context context, final T message) {
		super(context);
		init(context, message);

		if(!message.isRead()) {
			message.setRead(true);
			Map<String, String> data = new HashMap<>();
			data.put("message_id", message.getId());
			MetricModule.sendMetric(context, Event.EventLabel.message_center__read, null, data);
			post(new Runnable() {
				public void run() {
					MessageManager.updateMessage(context, message);
					MessageManager.notifyHostUnreadMessagesListener(MessageManager.getUnreadMessageCount(context));
				}
			});
		}
	}

	protected void init(Context context, T message) {
	}
}
