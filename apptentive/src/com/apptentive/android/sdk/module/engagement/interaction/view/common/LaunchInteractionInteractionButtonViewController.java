/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.common;

import android.content.Context;
import android.view.ViewGroup;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.LaunchInteractionAction;

/**
 * @author Sky Kelsey
 */
public class LaunchInteractionInteractionButtonViewController extends InteractionButtonViewController<LaunchInteractionAction> {
	public LaunchInteractionInteractionButtonViewController(Context context, ViewGroup parent, LaunchInteractionAction interactionButton) {
		super(context, parent, R.layout.apptentive_interaction_button_launch_interaction, interactionButton);
	}

	@Override
	public void init() {
	}
}
