/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.RatingDialogInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.view.common.ApptentiveDialogButton;

/**
 * @author Sky Kelsey
 */
public class RatingDialogInteractionView extends InteractionView<RatingDialogInteraction> {

	private static final String CODE_POINT_CANCEL = "cancel";
	private static final String CODE_POINT_RATE = "rate";
	private static final String CODE_POINT_REMIND = "remind";
	private static final String CODE_POINT_DECLINE = "decline";

	public RatingDialogInteractionView(RatingDialogInteraction interaction) {
		super(interaction);
	}

	@Override
	public void doOnCreate(final Activity activity, Bundle savedInstanceState) {
		activity.setContentView(R.layout.apptentive_rating_dialog_interaction);

		String title = interaction.getTitle();
		if (title != null) {
			TextView titleView = (TextView) activity.findViewById(R.id.title);
			titleView.setText(title);
		}

		TextView bodyView = (TextView) activity.findViewById(R.id.body);
		String body = interaction.getBody(activity);
		bodyView.setText(body);

		// Rate
		ApptentiveDialogButton rateButton = (ApptentiveDialogButton) activity.findViewById(R.id.rate);
		String rate = interaction.getRateText(activity);
		rateButton.setText(rate);
		rateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EngagementModule.engageInternal(activity, interaction, CODE_POINT_RATE);
				activity.finish();
			}
		});

		// Remind
		ApptentiveDialogButton remindButton = (ApptentiveDialogButton) activity.findViewById(R.id.remind);
		String remind = interaction.getRemindText();
		if (remind != null) {
			remindButton.setText(remind);
		}
		remindButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EngagementModule.engageInternal(activity, interaction, CODE_POINT_REMIND);
				activity.finish();
			}
		});

		// Decline
		ApptentiveDialogButton declineButton = (ApptentiveDialogButton) activity.findViewById(R.id.decline);
		String decline = interaction.getDeclineText();
		if (decline != null) {
			declineButton.setText(decline);
		}
		declineButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EngagementModule.engageInternal(activity, interaction, CODE_POINT_DECLINE);
				activity.finish();
			}
		});
	}

	@Override
	public boolean onBackPressed(Activity activity) {
		EngagementModule.engageInternal(activity, interaction, CODE_POINT_CANCEL);
		return true;
	}
}
