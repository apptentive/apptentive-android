/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.example.push;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.apptentive.android.example.ExampleActivity;
import com.apptentive.android.example.R;
import com.apptentive.android.sdk.Apptentive;
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

		Intent intent = new Intent(this, ExampleActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		Apptentive.setPendingPushNotification(getApplicationContext(), data);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.notification)
				.setContentTitle(title)
				.setContentText(body)
				.setAutoCancel(true)
				.setSound(defaultSoundUri)
				.setContentIntent(pendingIntent);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
	}
}
