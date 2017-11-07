/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveViewExitType;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.module.engagement.interaction.model.Invocation;
import com.apptentive.android.sdk.module.engagement.interaction.model.TextModalInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.Action;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.Actions;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.LaunchInteractionAction;
import com.apptentive.android.sdk.module.engagement.logic.FieldManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class NoteFragment extends ApptentiveBaseFragment<TextModalInteraction> {

	private final static int MAX_TEXT_LENGTH_FOR_ONE_BUTTONS = 19;
	private final static int MAX_TEXT_LENGTH_FOR_TWO_BUTTONS = 17;
	private final static int MAX_TEXT_LENGTH_FOR_THREE_BUTTONS = 15;
	private final static int MAX_TEXT_LENGTH_FOR_FOUR_BUTTONS = 11;

	public static NoteFragment newInstance(Bundle bundle) {
		NoteFragment fragment = new NoteFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.apptentive_textmodal_interaction_center, container, false);

		TextView title = (TextView) v.findViewById(R.id.title);
		if (interaction.getTitle() == null) {
			title.setVisibility(View.GONE);
		} else {
			title.setText(interaction.getTitle());
		}
		TextView body = (TextView) v.findViewById(R.id.body);
		if (interaction.getBody() == null) {
			body.setVisibility(View.GONE);
		} else {
			body.setText(interaction.getBody());
		}

		LinearLayout bottomArea = (LinearLayout) v.findViewById(R.id.button_container);
		Actions actionsObject = interaction.getActions();
		boolean vertical;
		List<Action> actions = null;
		if (actionsObject != null) {
			actions = actionsObject.getAsList();
		}
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
				Button button = (Button) inflater.inflate(R.layout.apptentive_textmodal_interaction_button, bottomArea, false);
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
									ApptentiveLog.e(e, "Error creating Event data object.");
								}
								engageInternal(TextModalInteraction.EVENT_NAME_DISMISS, data.toString());
								transit();
							}
						});
						break;
					case interaction:
						button.setActivated(true);
						button.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								LaunchInteractionAction launchInteractionButton = (LaunchInteractionAction) buttonAction;
								List<Invocation> invocations = launchInteractionButton.getInvocations();
								String interactionIdToLaunch = null;
								for (Invocation invocation : invocations) {
									FieldManager fieldManager = new FieldManager(getContext(), getConversation().getVersionHistory(), getConversation().getEventData(), getConversation().getPerson(), getConversation().getDevice(), getConversation().getAppRelease());
									if (invocation.isCriteriaMet(fieldManager)) {
										interactionIdToLaunch = invocation.getInteractionId();
										break;
									}
								}

								Interaction invokedInteraction = null;
								if (interactionIdToLaunch != null) {
									Conversation conversation = getConversation();
									if (conversation != null) {
										String interactionsString = conversation.getInteractions();
										if (interactionsString != null) {
											try {
												Interactions interactions = new Interactions(interactionsString);
												invokedInteraction = interactions.getInteraction(interactionIdToLaunch);
											}catch (JSONException e) {
												// Should never happen.
											}
										}
									}
								}

								JSONObject data = new JSONObject();
								try {
									data.put(TextModalInteraction.EVENT_KEY_ACTION_ID, buttonAction.getId());
									data.put(Action.KEY_LABEL, buttonAction.getLabel());
									data.put(TextModalInteraction.EVENT_KEY_ACTION_POSITION, position);
									data.put(TextModalInteraction.EVENT_KEY_INVOKED_INTERACTION_ID, invokedInteraction == null ? JSONObject.NULL : invokedInteraction.getId());
								} catch (JSONException e) {
									ApptentiveLog.e(e, "Error creating Event data object.");
								}

								engageInternal(TextModalInteraction.EVENT_NAME_INTERACTION, data.toString());
								if (invokedInteraction != null) {
									EngagementModule.launchInteraction(getActivity(), invokedInteraction);
								}
								transit();

							}
						});
						break;
				}
				bottomArea.addView(button);
			}
		} else {
			bottomArea.setVisibility(View.GONE);
		}
		return v;
	}

	@Override
	public boolean onFragmentExit(ApptentiveViewExitType exitType) {
		engageInternal(TextModalInteraction.EVENT_NAME_CANCEL, exitTypeToDataJson(exitType));
		return false;
	}
}
