/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import android.content.Context;
import android.content.Intent;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.notification.ApptentiveNotificationInteractionBroadcastReceiver;
import com.apptentive.android.sdk.util.NotificationUtils;

import static com.apptentive.android.sdk.ApptentiveLogTag.NOTIFICATIONS;
import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_ACTION_DISPLAY;
import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_CHANNEL_DEFAULT;
import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_EXTRA_INTERACTION_DEFINITION;
import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_EXTRA_INTERACTION_TYPE;

class NotificationInteractionLauncher implements InteractionLauncher {
	@Override
	public boolean launch(Context context, Interaction interaction) {
		if (!NotificationUtils.isNotificationChannelEnabled(context, NOTIFICATION_CHANNEL_DEFAULT)) {
			ApptentiveLog.e(NOTIFICATIONS, "Unable to engage notification interaction: notification channel is disabled");
			return false;
		}

		final Intent launchIntent = new Intent(context, ApptentiveNotificationInteractionBroadcastReceiver.class);
		launchIntent.setAction(NOTIFICATION_ACTION_DISPLAY);
		launchIntent.putExtra(NOTIFICATION_EXTRA_INTERACTION_TYPE, interaction.getType().name());
		launchIntent.putExtra(NOTIFICATION_EXTRA_INTERACTION_DEFINITION, interaction.toString());
		context.sendBroadcast(launchIntent);
		return true;
	}
}
