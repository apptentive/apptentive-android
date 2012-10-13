/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.TextMessage;
import com.apptentive.android.sdk.module.messagecenter.model.Message;
import com.apptentive.android.sdk.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class MessageCenterView extends FrameLayout {

	Context context;
	OnSendMessageListener onSendMessageListener;
	LinearLayout messageList;
	private List<Message> messages;
	EditText messageEditText;

	public MessageCenterView(Context context, OnSendMessageListener onSendMessageListener) {
		super(context);
		this.context = context;
		this.onSendMessageListener = onSendMessageListener;
		this.setId(R.id.apptentive_message_center_view);
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
		messageEditText = (EditText) findViewById(R.id.apptentive_message_center_message);
		Button send = (Button) findViewById(R.id.apptentive_message_center_send);
		send.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				String text = messageEditText.getText().toString().trim();
				if(text.length() == 0) {
					return;
				}
				messageEditText.setText("");
				Util.hideSoftKeyboard((Activity) context, messageEditText);
				onSendMessageListener.onSendMessage(text);
			}
		});
	}

	public void setMessages(List<Message> messages) {
		messageList.removeAllViews();
		this.messages = new ArrayList<Message>();
		for(Message message : messages) {
			addMessage(message);
		}
	}

	public void addMessage(Message message) {
		messages.add(message);
		switch (message.getTypeEnum()) {
			case text_message:
				messageList.addView(new TextMessageView(context, (TextMessage) message));
				break;
			case upgrade_request:
				break;
			case share_request:
				break;
			case unknown:
				break;
			default:
				break;
		}
	}

	public interface OnSendMessageListener {
		void onSendMessage(String text);
	}
}
