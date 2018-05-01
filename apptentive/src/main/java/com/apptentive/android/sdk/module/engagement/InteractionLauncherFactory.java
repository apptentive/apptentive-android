/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;

public interface InteractionLauncherFactory {
	InteractionLauncher launcherForInteraction(Interaction interaction);
}
