/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.ViewActivity;
import com.apptentive.android.sdk.module.messagecenter.model.Message;
import com.apptentive.android.sdk.module.messagecenter.model.TextMessage;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class ApptentiveMessageCenter {

	protected static MessageCenterView messageCenterView;

	public static void show(Context context) {
		messageCenterView = new MessageCenterView(context);

		Intent intent = new Intent();
		intent.setClass(context, ViewActivity.class);
		intent.putExtra("module", ViewActivity.Module.MESSAGE_CENTER.toString());
		context.startActivity(intent);
	}

	public static void doShow(Context context) {
		if (!(context instanceof Activity)) {
			Log.e(ApptentiveMessageCenter.class.getSimpleName() + " must be initialized with an Activity Context.");
			return;
		}

		List<Message> messages = new ArrayList<Message>();
		messages.add(new TextMessage("Hey, I'm having a problem with your app. It keeps crashing when I open up the map view. It's getting really frustrating.", false));
		messages.add(new TextMessage("Hi. I'm sorry to hear you're having problems with our app. We would like to try to solve this problem for you. I'll get back to you when we have a solution, OK?", true));
		messages.add(new TextMessage("Alright, we found the problem, and will be pushing out an update shortly.", true));
		messages.add(new TextMessage("Sweet, thanks. I will be looking forward to a fix.", false));
		messages.add(new TextMessage("Awesome! It works :)", false));
		messages.add(new TextMessage("Great!", true));
		messages.add(new TextMessage("Hey, I'm having a problem with your app. It keeps crashing when I open up the map view. It's getting really frustrating.", false));
		messages.add(new TextMessage("Hi. I'm sorry to hear you're having problems with our app. We would like to try to solve this problem for you. I'll get back to you when we have a solution, OK?", true));
		messages.add(new TextMessage("Alright, we found the problem, and will be pushing out an update shortly.", true));
		messages.add(new TextMessage("Sweet, thanks. I will be looking forward to a fix.", false));
		messages.add(new TextMessage("Awesome! It works :)", false));
		messages.add(new TextMessage("Great!", true));
		messages.add(new TextMessage("Hey, I'm having a problem with your app. It keeps crashing when I open up the map view. It's getting really frustrating.", false));
		messages.add(new TextMessage("Hi. I'm sorry to hear you're having problems with our app. We would like to try to solve this problem for you. I'll get back to you when we have a solution, OK?", true));
		messages.add(new TextMessage("Alright, we found the problem, and will be pushing out an update shortly.", true));
		messages.add(new TextMessage("Sweet, thanks. I will be looking forward to a fix.", false));
		messages.add(new TextMessage("Awesome! It works :)", false));
		messages.add(new TextMessage("Great!", true));
		messages.add(new TextMessage("Hey, I'm having a problem with your app. It keeps crashing when I open up the map view. It's getting really frustrating.", false));
		messages.add(new TextMessage("Hi. I'm sorry to hear you're having problems with our app. We would like to try to solve this problem for you. I'll get back to you when we have a solution, OK?", true));
		messages.add(new TextMessage("Alright, we found the problem, and will be pushing out an update shortly.", true));
		messages.add(new TextMessage("Sweet, thanks. I will be looking forward to a fix.", false));
		messages.add(new TextMessage("Awesome! It works :)", false));
		messages.add(new TextMessage("Great!", true));
		messageCenterView.setMessages(messages);

		((Activity) context).setContentView(messageCenterView);
	}

}
