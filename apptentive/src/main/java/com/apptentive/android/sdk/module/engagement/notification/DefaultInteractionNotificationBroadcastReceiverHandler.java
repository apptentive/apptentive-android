/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.ContextUtils;

import org.json.JSONException;

import java.util.Iterator;

import static com.apptentive.android.sdk.ApptentiveLogTag.NOTIFICATION_INTERACTIONS;
import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_CHANNEL_DEFAULT;
import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_EXTRA_INTERACTION_DEFINITION;

public class DefaultInteractionNotificationBroadcastReceiverHandler implements InteractionNotificationBroadcastReceiverHandler {

	private static final NoteInteractionNotificationAdapter DEFAULT_ADAPTER_NOTE = new NoteInteractionNotificationAdapter();

	@Override
	public void handleBroadcast(Context context, Intent intent) throws JSONException {
		ApptentiveLog.d(NOTIFICATION_INTERACTIONS, "Received broadcast");
		logIntent(intent);

		// Set Notification Channel if supported by version
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			ContextUtils.getNotificationManager(context).createNotificationChannel(NotificationChannelHolder.getInstance());
		}

		// Get the interaction to display
		Interaction.Type interactionType = Interaction.Type.parse(intent.getStringExtra(Constants.NOTIFICATION_EXTRA_INTERACTION_TYPE));
		String interactionDefinition = intent.getStringExtra(NOTIFICATION_EXTRA_INTERACTION_DEFINITION);
		if (interactionDefinition == null) {
			ApptentiveLog.w("Interaction Notification Intent is missing extra %s", NOTIFICATION_EXTRA_INTERACTION_DEFINITION);
			return;
		}

		InteractionNotificationAdapter interactionNotificationAdapter;
		switch (interactionType) {
			case TextModal:
				interactionNotificationAdapter = DEFAULT_ADAPTER_NOTE;
				break;
			default:
				ApptentiveLog.w("Attempted to launch Interaction as Notification, but that is not supported for the interaction type: %s", interactionDefinition);
				return;
		}
		interactionNotificationAdapter.handleInteractionNotificationAction(context, NOTIFICATION_CHANNEL_DEFAULT, intent);
	}

	private void logIntent(Intent intent) {
		if (ApptentiveLog.canLog(ApptentiveLog.Level.VERBOSE)) {
			String action = intent.getAction();
			ApptentiveLog.v(NOTIFICATION_INTERACTIONS, "Action: %s", action);
			Bundle extras = intent.getExtras();
			if (extras != null) {
				Iterator<String> extraKeys = extras.keySet().iterator();
				ApptentiveLog.v(NOTIFICATION_INTERACTIONS, "Extras:");
				while (extraKeys.hasNext()) {
					String key = extraKeys.next();
					ApptentiveLog.v(NOTIFICATION_INTERACTIONS, "  \"%s\" = \"%s\"", key, String.valueOf(extras.get(key)));
				}
			}
		}
	}
}
