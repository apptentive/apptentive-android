/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.apptentive.android.sdk.Apptentive;
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
	public void show(final Activity activity) {
		super.show(activity);
		// TODO: Send proper metric // MetricModule.sendMetric(activity, Event.EventLabel.enjoyment_dialog__launch);
		activity.setContentView(R.layout.apptentive_enjoyment_dialog_interaction);

		String body = interaction.getBody();
		if (body != null) {
			TextView bodyView = (TextView) activity.findViewById(R.id.body);
			bodyView.setText(body);
		}

		// No
		String noText = interaction.getNoText();
		Button noButton = (Button) activity.findViewById(R.id.no);
		if (noText != null) {
			noButton.setText(noText);
		}
		noButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Apptentive.engageInternal(activity, "enjoyment_dialog", "no");
				activity.finish();
			}
		});

		// Yes
		String yesText = interaction.getYesText();
		Button yesButton = (Button) activity.findViewById(R.id.yes);
		if (yesText != null) {
			yesButton.setText(yesText);
		}
		yesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Apptentive.engageInternal(activity, "enjoyment_dialog", "yes");
				activity.finish();
			}
		});
	}

	@Override
	public void onStop() {

	}

	@Override
	public void onBackPressed(Activity activity) {
		Apptentive.engageInternal(activity, "enjoyment_dialog", "dismiss");
	}
}
