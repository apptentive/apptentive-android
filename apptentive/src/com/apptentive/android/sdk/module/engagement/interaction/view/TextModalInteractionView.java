/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.TextModalInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.Action;

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

//	EngagementModule.engageInternal(activity, interaction.getType().name(), TextModalInteraction.EVENT_NAME_LAUNCH);
		Integer primaryColor = interaction.getPrimaryColor();
		if (primaryColor != null) {
			View topArea = activity.findViewById(R.id.top_area);
			GradientDrawable backgroundDrawable = (GradientDrawable) topArea.getBackground();
			backgroundDrawable.setColor(primaryColor);
		} else {
			// Changing the background color affects this background if it is reused later. Reset it to defaults.
			View topArea = activity.findViewById(R.id.top_area);
			GradientDrawable backgroundDrawable = (GradientDrawable) topArea.getBackground();
			backgroundDrawable.setColor(activity.getResources().getColor(R.color.apptentive_dialog_primary));
		}

		TextView title = (TextView) activity.findViewById(R.id.title);
		title.setText(interaction.getTitle());
		TextView body = (TextView) activity.findViewById(R.id.body);
		body.setText(interaction.getBody());
		Integer textColor = interaction.getTextColor();
		if (textColor != null) {
			title.setTextColor(textColor);
			body.setTextColor(textColor);
		}

		LinearLayout bottomArea = (LinearLayout) activity.findViewById(R.id.bottom_area);
		Integer secondaryColor = interaction.getSecondaryColor();
		if (primaryColor != null) {
			GradientDrawable backgroundDrawable = (GradientDrawable) bottomArea.getBackground();
			backgroundDrawable.setColor(secondaryColor);
		} else {
			// Changing the background color affects this background if it is reused later. Reset it to defaults.
			GradientDrawable backgroundDrawable = (GradientDrawable) bottomArea.getBackground();
			backgroundDrawable.setColor(activity.getResources().getColor(R.color.apptentive_dialog_secondary));
		}

		LayoutInflater inflater = activity.getLayoutInflater();
		List<Action> buttons = interaction.getInteractionButtons().getAsList();
		boolean vertical = false;
		if (buttons != null && !buttons.isEmpty()) {
			if (buttons.size() > 4) {
				vertical = true;
			} else if (buttons.size() == 1) {
				vertical = true;
			} else {
				int totalChars = 0;
				for (Action button : buttons) {
					totalChars += button.getLabel().length();
				}
				vertical = totalChars > MAX_TEXT_LENGTH_FOR_TWO_BUTTONS;
			}
			if (vertical) {
				Log.e("Setting orientation to VERTICAL");
				bottomArea.setOrientation(LinearLayout.VERTICAL);
			} else {
				Log.e("Setting orientation to HORIZONTAL");
				bottomArea.setOrientation(LinearLayout.HORIZONTAL);
			}
			for (Action button : buttons) {
				View buttonView = null;
				Log.e("Adding: %s", button.getLabel());
				buttonView = inflater.inflate(R.layout.apptentive_dialog_button, bottomArea, false);
				TextView buttonTextView = ((TextView) buttonView.findViewById(R.id.label));
				buttonTextView.setText(button.getLabel());
				Integer buttonTextColor = interaction.getButtonTextColor();
				if (buttonTextColor != null) {
					buttonTextView.setTextColor(buttonTextColor);
				}
				switch (button.getType()) {
					case Dismiss:
						buttonView.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								Log.e("Dismiss Button Clicked.");
//							EngagementModule.engageInternal(activity, interaction.getType().name(), TextModalInteraction.EVENT_NAME_DISMISS);
								activity.finish();
							}
						});
						break;
					case Interaction:
						buttonView.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								Log.e("Interaction Button Clicked.");
//							EngagementModule.engageInternal(activity, interaction.getType().name(), TextModalInteraction.EVENT_NAME_INTERACTION);
							}
						});
						break;
				}
				bottomArea.addView(buttonView);
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
//	EngagementModule.engageInternal(activity, interaction.getType().name(), TextModalInteraction.EVENT_NAME_CANCEL);
		return true;
	}
}
