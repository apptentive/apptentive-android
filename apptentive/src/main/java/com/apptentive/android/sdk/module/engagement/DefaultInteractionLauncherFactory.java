/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import androidx.annotation.NonNull;

import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction.DisplayType;

public class DefaultInteractionLauncherFactory implements InteractionLauncherFactory {
	DefaultInteractionLauncherFactory() {
	}

	@Override
	public InteractionLauncher launcherForInteraction(Interaction interaction) {
		final Interaction.Type type = interaction.getType();
		if (Interaction.Type.TextModal.equals(type)) {
			return DisplayType.notification.equals(interaction.getDisplayType())
					? createNotificationInteractionLauncher()
					: createActivityInteractionLauncher();
		}

		if (Interaction.Type.InAppRatingDialog.equals(type)) {
			return createInAppRatingDialogInteractionLauncher();
		}

		return createActivityInteractionLauncher();
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

	@NonNull
	InteractionLauncher createInAppRatingDialogInteractionLauncher() {
		return new InAppRatingDialogInteractionLauncher();
	}
}
