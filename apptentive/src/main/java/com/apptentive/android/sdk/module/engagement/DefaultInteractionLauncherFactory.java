/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import androidx.annotation.NonNull;

import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction.DisplayType;

import java.util.HashMap;
import java.util.Map;

public class DefaultInteractionLauncherFactory implements InteractionLauncherFactory {
	private final Map<DisplayType, InteractionLauncher> launcherLookup;

	DefaultInteractionLauncherFactory() {
		launcherLookup = createLauncherLookup();
	}

	private Map<DisplayType, InteractionLauncher> createLauncherLookup() {
		Map<DisplayType, InteractionLauncher> lookup = new HashMap<>();
		lookup.put(DisplayType.notification, createNotificationInteractionLauncher());
		// This is for maintaining existing behavior
		lookup.put(DisplayType.unknown, createActivityInteractionLauncher());
		return lookup;
	}

	@Override
	public InteractionLauncher launcherForInteraction(Interaction interaction) {
		return launcherLookup.get(interaction.getDisplayType());
	}

	// for Unit-tests

	@NonNull
	InteractionLauncher createActivityInteractionLauncher() {
		return new ActivityInteractionLauncher();
	}

	@NonNull
	InteractionLauncher createNotificationInteractionLauncher() {
		return new NotificationInteractionLauncher();
	}
}
