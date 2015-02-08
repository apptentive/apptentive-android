/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import android.os.Bundle;
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
import com.apptentive.android.sdk.module.engagement.interaction.view.common.ApptentiveDialogButton;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public class TextModalInteractionView extends InteractionView<TextModalInteraction> {

	@SuppressWarnings("unused")
	private final static int MAX_TEXT_LENGTH_FOR_ONE_BUTTONS = 19;
	private final static int MAX_TEXT_LENGTH_FOR_TWO_BUTTONS = 17;
	private final static int MAX_TEXT_LENGTH_FOR_THREE_BUTTONS = 15;
	private final static int MAX_TEXT_LENGTH_FOR_FOUR_BUTTONS = 11;

	public TextModalInteractionView(TextModalInteraction interaction) {
		super(interaction);
	}

	@Override
	public void doOnCreate(final Activity activity, Bundle onSavedInstanceState) {
		activity.setContentView(R.layout.apptentive_textmodal_interaction_center);


		TextView title = (TextView) activity.findViewById(R.id.title);
		if (interaction.getTitle() == null) {
			title.setVisibility(View.GONE);
		} else {
			title.setText(interaction.getTitle());
		}
		TextView body = (TextView) activity.findViewById(R.id.body);
		if (interaction.getBody() == null) {
			body.setVisibility(View.GONE);
		} else {
			body.setText(interaction.getBody());
		}

		LinearLayout bottomArea = (LinearLayout) activity.findViewById(R.id.bottom_area);
		List<Action> actions = interaction.getActions().getAsList();
		boolean vertical;
		if (actions != null && !actions.isEmpty()) {
			int totalChars = 0;
			for (Action button : actions) {
				totalChars += button.getLabel().length();
			}
			if (actions.size() == 1) {
				vertical = false;
			} else if (actions.size() == 2) {
				vertical = totalChars > MAX_TEXT_LENGTH_FOR_TWO_BUTTONS;
			} else if (actions.size() == 3) {
				vertical = totalChars > MAX_TEXT_LENGTH_FOR_THREE_BUTTONS;
			} else if (actions.size() == 4) {
				vertical = totalChars > MAX_TEXT_LENGTH_FOR_FOUR_BUTTONS;
			} else {
				vertical = true;
			}
			if (vertical) {
				bottomArea.setOrientation(LinearLayout.VERTICAL);
			} else {
				bottomArea.setOrientation(LinearLayout.HORIZONTAL);
			}

			for (int i = 0; i < actions.size(); i++) {
				final Action buttonAction = actions.get(i);
				final int position = i;
				ApptentiveDialogButton button = new ApptentiveDialogButton(activity);
				button.setText(buttonAction.getLabel());
				switch (buttonAction.getType()) {
					case dismiss:
						button.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								JSONObject data = new JSONObject();
								try {
									data.put(TextModalInteraction.EVENT_KEY_ACTION_ID, buttonAction.getId());
									data.put(Action.KEY_LABEL, buttonAction.getLabel());
									data.put(TextModalInteraction.EVENT_KEY_ACTION_POSITION, position);
								} catch (JSONException e) {
									Log.e("Error creating Event data object.", e);
								}
								EngagementModule.engageInternal(activity, interaction, TextModalInteraction.EVENT_NAME_DISMISS, data.toString());
								activity.finish();
							}
						});
						break;
					case interaction:
						button.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								LaunchInteractionAction launchInteractionButton = (LaunchInteractionAction) buttonAction;
								List<Invocation> invocations = launchInteractionButton.getInvocations();
								String interactionIdToLaunch = null;
								for (Invocation invocation : invocations) {
									if (invocation.isCriteriaMet(activity)) {
										interactionIdToLaunch = invocation.getInteractionId();
										break;
									}
								}

								Interaction invokedInteraction = null;
								if (interactionIdToLaunch != null) {
									Interactions interactions = InteractionManager.getInteractions(activity);
									if (interactions != null) {
										invokedInteraction = interactions.getInteraction(interactionIdToLaunch);
									}
								}

								JSONObject data = new JSONObject();
								try {
									data.put(TextModalInteraction.EVENT_KEY_ACTION_ID, buttonAction.getId());
									data.put(Action.KEY_LABEL, buttonAction.getLabel());
									data.put(TextModalInteraction.EVENT_KEY_ACTION_POSITION, position);
									data.put(TextModalInteraction.EVENT_KEY_INVOKED_INTERACTION_ID, invokedInteraction == null ? JSONObject.NULL : invokedInteraction.getId());
								} catch (JSONException e) {
									Log.e("Error creating Event data object.", e);
								}

								EngagementModule.engageInternal(activity, interaction, TextModalInteraction.EVENT_NAME_INTERACTION, data.toString());
								if (invokedInteraction != null) {
									EngagementModule.launchInteraction(activity, invokedInteraction);
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
	public boolean onBackPressed(Activity activity) {
		EngagementModule.engageInternal(activity, interaction, TextModalInteraction.EVENT_NAME_CANCEL);
		return true;
	}
}
