/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.debug;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.apptentive.android.sdk.R;

import java.io.File;

import static android.content.Context.NOTIFICATION_SERVICE;

final class TroubleshootingNotificationBuilder {

	private static final String NOTIFICATION_CHANNEL_ID = "com.apptentive.debug.NOTIFICATION_CHANNEL_TROUBLESHOOTING";
	private static final String NOTIFICATION_CHANNEL_NAME = "Apptentive Notifications";
	private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Used for SDK troubleshooting";
	static final String NOTIFICATION_ID_KEY = "com.apptentive.debug.NOTIFICATION_ID";
	private static final int APPTENTIVE_NOTIFICATION_ID = 1;

	static final String ACTION_ABORT = "com.apptentive.debug.ACTION_ABORT";
	static final String ACTION_SEND_LOGS = "com.apptentive.debug.ACTION_SEND_LOGS";

	static final String EXTRA_EMAIL_RECIPIENTS = "EMAIL_RECIPIENTS";
	static final String EXTRA_SUBJECT = "SUBJECT";
	static final String EXTRA_INFO = "INFO";
	static final String EXTRA_ATTACHMENTS = "ATTACHMENTS";

	private TroubleshootingNotificationBuilder() {
	}

	static Notification buildNotification(@NonNull Context context, String subject, String systemInfo, File[] attachments, String[] emailRecipients) {

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

		// Set up PendingIntents for actions
		Intent abortIntent = new Intent(context, LogBroadcastReceiver.class);
		abortIntent.setAction(ACTION_ABORT);
		abortIntent.putExtra(NOTIFICATION_ID_KEY, APPTENTIVE_NOTIFICATION_ID);
		PendingIntent abortPendingIntent = PendingIntent.getBroadcast(context, 0, abortIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Action abortAction = new NotificationCompat.Action.Builder(0, "Discard", abortPendingIntent).build();

		Intent sendLogsIntent = new Intent(context, LogBroadcastReceiver.class);
		sendLogsIntent.setAction(ACTION_SEND_LOGS);
		sendLogsIntent.putExtra(NOTIFICATION_ID_KEY, APPTENTIVE_NOTIFICATION_ID);
		sendLogsIntent.putExtra(EXTRA_EMAIL_RECIPIENTS, emailRecipients);
		sendLogsIntent.putExtra(EXTRA_SUBJECT, subject);
		sendLogsIntent.putExtra(EXTRA_INFO, systemInfo);
		sendLogsIntent.putExtra(EXTRA_ATTACHMENTS, attachments);

		PendingIntent sendLogsPendingIntent = PendingIntent.getBroadcast(context, 0, sendLogsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Action sendLogsAction = new NotificationCompat.Action.Builder(0, "Send Report", sendLogsPendingIntent).build();

		// Build notification
		final NotificationCompat.Builder builder =
				new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
						.setDefaults(Notification.DEFAULT_SOUND)
						.setOnlyAlertOnce(true)
						.setOngoing(true)
						.setAutoCancel(false)
						.setSmallIcon(R.drawable.apptentive_status_gear)
						.setSubText("Apptentive SDK")
						.setContentTitle("Troubleshooting Mode")
						.setContentText("Reproduce your problem, then send report")
						.addAction(abortAction)
						.addAction(sendLogsAction)
						.setWhen(System.currentTimeMillis())
						.setVibrate(new long[]{0, 100, 80, 240, 500, 100, 80, 240})
						.setLights(Color.RED, 200, 400);

		// Pre-Jelly Bean versions don't support Notification Actions. Just send logs when notification is tapped.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			builder.setContentIntent(sendLogsPendingIntent);
			builder.setContentText("Tap to send logs");
		}

		// Tinting the notification icon added in Marshmallow
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			builder.setColor(context.getResources().getColor(R.color.apptentive_brand_red, (Resources.Theme) null));
		}

		// A Notification Channel is required starting in Oreo
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
			notificationChannel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
			// These are required in the channel in order to let notifications vibrate and use lights when targeting API 26+
			notificationChannel.enableLights(true);
			notificationChannel.setLightColor(Color.RED);
			notificationChannel.setVibrationPattern(new long[]{0, 100, 80, 240, 500, 100, 80, 240});

			notificationManager.createNotificationChannel(notificationChannel);
		}

		return builder.build();
	}
}
