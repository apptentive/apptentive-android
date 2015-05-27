/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem;

/**
 * @author Sky Kelsey
 */
public class MessageAdapter<T extends MessageCenterListItem> extends ArrayAdapter<T> {

	public MessageAdapter(Context context) {
		super(context, 0);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MessageCenterListItem listItem = getItem(position);
		if (listItem instanceof Message) {
			Message message = (Message) listItem;
			if (message.getBaseType() == Payload.BaseType.message) {
				switch (message.getType()) {
					case TextMessage:
						return new TextMessageView(parent.getContext(), (TextMessage) message);
					case FileMessage:
						return new FileMessageView(parent.getContext(), (FileMessage) message);
					case AutomatedMessage:
						return new AutomatedMessageView(parent.getContext(), (AutomatedMessage) message);
					default:
						Log.a("Unrecognized message type: %s", message.getType());
						return null;
				}
			}
			Log.d("Can't render non-Message Payload as Message: %s", message.getType());
			return null;
		} else if (listItem instanceof MessageCenterGreeting) {
			return new MessageCenterGreetingView(parent.getContext(), (MessageCenterGreeting) listItem);
		} else {
			Log.d("Unregocnized MessageCenterListItem: %s", listItem.getClass().getSimpleName());
			return null;
		}


	}
}
