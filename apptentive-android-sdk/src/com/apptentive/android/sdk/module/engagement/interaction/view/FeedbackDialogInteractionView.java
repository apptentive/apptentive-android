/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import android.view.View;
import android.widget.*;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.FeedbackDialogInteraction;

/**
 * @author Sky Kelsey
 */
public class FeedbackDialogInteractionView extends InteractionView<FeedbackDialogInteraction> {

	private static final String CODE_POINT_LAUNCH = "launch";
	private static final String CODE_POINT_DISMISS = "dismiss";
	private static final String CODE_POINT_NO = "no";
	private static final String CODE_POINT_SEND = "send";
	private static final String CODE_POINT_VIEW_MESSAGES = "view_messages"; // TODO: Use this.

	private CharSequence email;

	public FeedbackDialogInteractionView(FeedbackDialogInteraction interaction) {
		super(interaction);
	}

	@Override
	public void show(final Activity activity) {
		super.show(activity);
		activity.setContentView(R.layout.apptentive_feedback_dialog_interaction);

		Apptentive.engageInternal(activity, interaction.getType().name(), CODE_POINT_LAUNCH);

		String title = interaction.getTitle();
		if (title != null) {
			TextView titleView = (TextView) activity.findViewById(R.id.title);
			titleView.setText(title);
		}

		String body = interaction.getBody();
		if (body != null) {
			TextView bodyView = (TextView) activity.findViewById(R.id.body);
			bodyView.setText(body);
		}

		EditText emailView = (EditText) activity.findViewById(R.id.email);
		String emailHintText = interaction.getEmailHintText();
		if (emailHintText != null) {
			emailView.setHint(emailHintText);
		}

		EditText messageView = (EditText) activity.findViewById(R.id.message);
		String messageHintText = interaction.getMessageHintText();
		if (messageHintText != null) {
			messageView.setHint(messageHintText);
		}

		// No
		Button noButton = (Button) activity.findViewById(R.id.no_thanks);
		String no = interaction.getNoText();
		if (no != null) {
			noButton.setText(no);
		}
		noButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Apptentive.engageInternal(activity, interaction.getType().name(), CODE_POINT_NO);
				activity.finish();
			}
		});


		// Send
		Button sendButton = (Button) activity.findViewById(R.id.send);
		String send = interaction.getSendText();
		if (send != null) {
			sendButton.setText(send);
		}
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Apptentive.engageInternal(activity, interaction.getType().name(), CODE_POINT_SEND);
				activity.finish();
			}
		});

	}

	@Override
	public void onStop() {

	}

	@Override
	public void onBackPressed(Activity activity) {
		Apptentive.engageInternal(activity, interaction.getType().name(), CODE_POINT_DISMISS);
		activity.finish();
	}
}
