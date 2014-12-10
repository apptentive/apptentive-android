/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.TextModalInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.DismissInteractionButton;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.InteractionButton;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.LaunchInteractionInteractionButton;
import com.apptentive.android.sdk.module.engagement.interaction.view.common.DismissInteractionButtonViewController;
import com.apptentive.android.sdk.module.engagement.interaction.view.common.InteractionButtonViewController;
import com.apptentive.android.sdk.module.engagement.interaction.view.common.LaunchInteractionInteractionButtonViewController;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public class TextModalInteractionView extends InteractionView<TextModalInteraction> {

	private static final String EVENT_NAME_LAUNCH = "launch";
	private static final String EVENT_NAME_CANCEL = "cancel";
	private static final String EVENT_NAME_DISMISS = "dismiss";
	private static final String EVENT_NAME_INTERACTION_CANCEL = "interaction";

	public TextModalInteractionView(TextModalInteraction interaction) {
		super(interaction);
	}

	@Override
	public void show(final Activity activity) {
		super.show(activity);
		activity.setContentView(R.layout.apptentive_textmodal_interaction);

//		EngagementModule.engageInternal(activity, interaction.getType().name(), EVENT_NAME_LAUNCH);

		TextView title = (TextView) activity.findViewById(R.id.title);
		title.setText(interaction.getTitle());

		TextView body = (TextView) activity.findViewById(R.id.body);
		body.setText(interaction.getBody());

		ViewGroup buttonContainer = (ViewGroup) activity.findViewById(R.id.buttons);

		List<InteractionButton> buttons = interaction.getInteractionButtons().getAsList();
		for (InteractionButton button : buttons) {
			InteractionButtonViewController buttonView = null;
			switch (button.getAction()) {
				case Dismiss:
					buttonView = new DismissInteractionButtonViewController(activity, buttonContainer, (DismissInteractionButton) button);
					buttonView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							Log.e("Dismiss Button Clicked.");
//							EngagementModule.engageInternal(activity, interaction.getType().name(), EVENT_NAME_DISMISS);
							activity.finish();
						}
					});
					break;
				case Interaction:
					buttonView = new LaunchInteractionInteractionButtonViewController(activity, buttonContainer, (LaunchInteractionInteractionButton) button);
					buttonView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							Log.e("Interaction Button Clicked.");
//							EngagementModule.engageInternal(activity, interaction.getType().name(), EVENT_NAME_INTERACTION);
						}
					});
					break;
			}
			if (buttonView != null) {
				buttonContainer.addView(buttonView.getButton());
			}
		}
	}

	@Override
	public void onStop() {

	}

	@Override
	public boolean onBackPressed(Activity activity) {
//		EngagementModule.engageInternal(activity, interaction.getType().name(), EVENT_NAME_CANCEL);
		return true;
	}
}
