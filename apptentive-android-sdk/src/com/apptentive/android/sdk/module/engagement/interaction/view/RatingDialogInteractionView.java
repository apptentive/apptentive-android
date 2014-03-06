/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import android.widget.Button;
import android.widget.TextView;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.module.engagement.interaction.model.RatingDialogInteraction;

/**
 * @author Sky Kelsey
 */
public class RatingDialogInteractionView extends InteractionView<RatingDialogInteraction> {

	public RatingDialogInteractionView(RatingDialogInteraction interaction) {
		super(interaction);
	}

	@Override
	public void show(Activity activity) {
		super.show(activity);
		activity.setContentView(R.layout.apptentive_rating_dialog_interaction);

		String title = interaction.getTitle();
		if (title != null) {
			TextView titleView = (TextView) activity.findViewById(R.id.title);
			titleView.setText(title);
		}

		TextView bodyView = (TextView) activity.findViewById(R.id.body);
		String body = interaction.getBody();
		if (body == null) {
			body = String.format(activity.getResources().getString(R.string.apptentive_rating_message_fs), Configuration.load(activity).getAppDisplayName());
		}
		bodyView.setText(body);

		Button rateView = (Button) activity.findViewById(R.id.rate);
		String rate = interaction.getRateText();
		if (rate == null) {
			rate = String.format(activity.getResources().getString(R.string.apptentive_rate_this_app), Configuration.load(activity).getAppDisplayName());
		}
		rateView.setText(rate);

		String remind = interaction.getRemindText();
		if (remind != null) {
			Button remindButton = (Button) activity.findViewById(R.id.remind);
			remindButton.setText(remind);
		}

		String no = interaction.getNoText();
		if (no != null) {
			Button noButton = (Button) activity.findViewById(R.id.no);
			noButton.setText(no);
		}
	}

	@Override
	public void onStop() {
	}

	@Override
	public void onBackPressed() {
	}
}
