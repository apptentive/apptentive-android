/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.apptentive.android.sdk.ApptentiveLog;

import static com.apptentive.android.sdk.ApptentiveLogTag.NOTIFICATION_INTERACTIONS;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class ApptentiveNotificationInteractionBroadcastReceiver extends BroadcastReceiver {

	private static final InteractionNotificationBroadcastReceiverHandler DEFAULT_HANDLER = new DefaultInteractionNotificationBroadcastReceiverHandler();

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			DEFAULT_HANDLER.handleBroadcast(context, intent);
		} catch (Exception e) {
			ApptentiveLog.w(NOTIFICATION_INTERACTIONS, e, "Error handling Apptentive Interaction Notification broadcast.");
			logException(e);
		}
	}
}
