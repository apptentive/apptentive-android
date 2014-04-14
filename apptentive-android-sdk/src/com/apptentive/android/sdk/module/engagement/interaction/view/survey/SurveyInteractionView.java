/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.app.Activity;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.SurveyInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.view.InteractionView;

/**
 * @author Sky Kelsey
 */
public class SurveyInteractionView extends InteractionView<SurveyInteraction> {

	public SurveyInteractionView(SurveyInteraction interaction) {
		super(interaction);
	}

	@Override
	public void show(Activity activity) {
		super.show(activity);
		activity.finish();
	}

	@Override
	public void onStop() {

	}

	@Override
	public void onBackPressed(Activity activity) {

	}
}
