/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.common;

import android.content.Context;
import android.view.ViewGroup;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.DismissAction;

/**
 * @author Sky Kelsey
 */
public class DismissInteractionButtonViewController extends InteractionButtonViewController<DismissAction> {
	public DismissInteractionButtonViewController(Context context, ViewGroup parent, DismissAction interactionButton) {
		super(context, parent, R.layout.apptentive_interaction_button_dismiss, interactionButton);
	}

	protected void init() {
	}
}
