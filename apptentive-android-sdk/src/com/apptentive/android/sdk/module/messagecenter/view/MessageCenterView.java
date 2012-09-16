/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.Message;
import com.apptentive.android.sdk.module.messagecenter.model.TextMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class MessageCenterView extends FrameLayout {

	Context context;
	LinearLayout messageList;
	private List<Message> messages;

	public MessageCenterView(Context context) {
		super(context);
		this.context = context;
		setup();
	}

	protected void setup() {
		if (!(context instanceof Activity)) {
			Log.e(this.getClass().getSimpleName() + " must be initialized with an Activity Context.");
			return;
		}
		messages = new ArrayList<Message>();

		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		inflater.inflate(R.layout.apptentive_message_center, this);

		messageList = (LinearLayout) findViewById(R.id.aptentive_message_center_list);

	}

	public void setMessages(List<Message> messages) {
		for (int i = 0; i < messages.size(); i++) {
			Message message =  messages.get(i);
			addMessage(message);
		}
	}

	public void addMessage(Message message) {
		messages.add(message);
		if(message instanceof TextMessage) {
			messageList.addView(new TextMessageView(context, (TextMessage) message));
		}
	}
}
