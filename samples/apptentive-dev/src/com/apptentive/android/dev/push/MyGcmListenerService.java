/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.dev.push;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.apptentive.android.dev.MainActivity;
import com.apptentive.android.dev.R;
import com.apptentive.android.sdk.Log;
import com.google.android.gms.gcm.GcmListenerService;

/**
 * @author Sky Kelsey
 */
public class MyGcmListenerService extends GcmListenerService {
	@Override
	public void onMessageReceived(String from, Bundle data) {
		String title = data.getString("gcm.notification.title");
		String body = data.getString("gcm.notification.body");
		Log.e("From: " + from);
		Log.e("Title: " + title);
		Log.e("Body: " + body);

		/**
		 * Production applications would usually process the message here.
		 * Eg: - Syncing with server.
		 *     - Store message in local database.
		 *     - Update UI.
		 */

		/**
		 * In some cases it may be useful to show a notification indicating to the user
		 * that a message was received.
		 */
		sendNotification(title, body);
	}

	/**
	 * Create and show a simple notification containing the received GCM message.
	 *
	 * @param title GCM message title received.
	 * @param body  GCM message body received.
	 */
	private void sendNotification(String title, String body) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
				PendingIntent.FLAG_ONE_SHOT);

		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.notification)
				.setContentTitle(title)
				.setContentText(body)
				.setAutoCancel(true)
				.setSound(defaultSoundUri)
				.setContentIntent(pendingIntent);

		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
	}
}
