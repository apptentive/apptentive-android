/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.module.engagement.interaction.model.Invocation;
import com.apptentive.android.sdk.module.engagement.interaction.model.TextModalInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.Action;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.LaunchInteractionAction;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public class TextModalInteractionView extends InteractionView<TextModalInteraction> {

	private final static int MAX_TEXT_LENGTH_FOR_TWO_BUTTONS = 21;

	public TextModalInteractionView(TextModalInteraction interaction) {
		super(interaction);
	}

	@Override
	public void show(final Activity activity) {
		super.show(activity);
		switch (interaction.getLayout()) {
			case center:
				activity.setContentView(R.layout.apptentive_textmodal_interaction_center);
				break;
			case bottom:
				activity.setContentView(R.layout.apptentive_textmodal_interaction_bottom);
				break;
			default:
				activity.setContentView(R.layout.apptentive_textmodal_interaction_center);
				break;
		}

		EngagementModule.engageInternal(activity, interaction, TextModalInteraction.EVENT_NAME_LAUNCH);
		TextView title = (TextView) activity.findViewById(R.id.title);
		title.setText(interaction.getTitle());
		TextView body = (TextView) activity.findViewById(R.id.body);
		body.setText(interaction.getBody());

		LinearLayout bottomArea = (LinearLayout) activity.findViewById(R.id.bottom_area);
		LayoutInflater inflater = activity.getLayoutInflater();
		List<Action> actions = interaction.getActions().getAsList();
		boolean vertical;
		if (actions != null && !actions.isEmpty()) {
			if (actions.size() > 4) {
				vertical = true;
			} else if (actions.size() == 1) {
				vertical = true;
			} else {
				int totalChars = 0;
				for (Action button : actions) {
					totalChars += button.getLabel().length();
				}
				vertical = totalChars > MAX_TEXT_LENGTH_FOR_TWO_BUTTONS;
			}
			if (vertical) {
				bottomArea.setOrientation(LinearLayout.VERTICAL);
			} else {
				bottomArea.setOrientation(LinearLayout.HORIZONTAL);
			}
			for (int i = 0; i < actions.size(); i++) {
				final Action buttonAction = actions.get(i);
				final int position = i;
				View button;
				button = inflater.inflate(R.layout.apptentive_dialog_button, bottomArea, false);
				TextView buttonTextView = ((TextView) button.findViewById(R.id.label));
				buttonTextView.setText(buttonAction.getLabel());
				switch (buttonAction.getType()) {
					case dismiss:
						button.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								Log.e("Dismiss Button Clicked.");
								String data = String.format("{\"title\":\"%s\",\"position\":%d}", buttonAction.getLabel(), position);
								EngagementModule.engageInternal(activity, interaction, TextModalInteraction.EVENT_NAME_DISMISS, data);
								activity.finish();
							}
						});
						break;
					case interaction:
						button.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								Log.e("Interaction Button Clicked.");
								LaunchInteractionAction launchInteractionButton = (LaunchInteractionAction) buttonAction;
								List<Invocation> invocations = launchInteractionButton.getInvocations();
								String interactionIdToLaunch = null;
								for (Invocation invocation : invocations) {
									if (invocation.isCriteriaMet(activity)) {
										interactionIdToLaunch = invocation.getInteractionId();
										break;
									}
								}
								if (interactionIdToLaunch != null) {
									Interactions interactions = InteractionManager.getInteractions(activity);
									if (interactions != null) {
										Interaction interaction = interactions.getInteraction(interactionIdToLaunch);
										if (interaction != null) {
											String data = String.format("{\"title\":\"%s\",\"position\":%d,\"target\":\"%s\"}", buttonAction.getLabel(), position, interaction.getId());
											EngagementModule.engageInternal(activity, interaction, TextModalInteraction.EVENT_NAME_INTERACTION, data);
											EngagementModule.launchInteraction(activity, interaction);
										}
									}
								} else {
									Log.w("No Interactions were launched.");
								}
								activity.finish();
							}
						});
						break;
				}
				bottomArea.addView(button);
			}
		} else {
			bottomArea.setVisibility(View.GONE);
		}
	}

	@Override
	public void onStop() {

	}

	@Override
	public boolean onBackPressed(Activity activity) {
		EngagementModule.engageInternal(activity, interaction, TextModalInteraction.EVENT_NAME_CANCEL);
		return true;
	}
}
