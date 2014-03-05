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
import com.apptentive.android.sdk.module.engagement.interaction.model.EnjoymentDialogInteraction;

/**
 * @author Sky Kelsey
 */
public class EnjoymentDialogInteractionView extends InteractionView<EnjoymentDialogInteraction> {


	public EnjoymentDialogInteractionView(EnjoymentDialogInteraction interaction) {
		super(interaction);
	}

	@Override
	public void show(Activity activity) {
		super.show(activity);
		activity.setContentView(R.layout.apptentive_enjoyment_dialog_interaction);

		String body = interaction.getBody();
		if (body != null) {
			TextView bodyView = (TextView) activity.findViewById(R.id.body);
			bodyView.setText(body);
		}

		String noText = interaction.getNoText();
		Button noButton = (Button) activity.findViewById(R.id.no);
		if (noText != null) {
			noButton.setText(noText);
		}

		String yesText = interaction.getYesText();
		Button yesButton = (Button) activity.findViewById(R.id.yes);
		if (yesText != null) {
			yesButton.setText(yesText);
		}
	}

	@Override
	public void onStop() {

	}

	@Override
	public void onBackPressed() {

	}
}
