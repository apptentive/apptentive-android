/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.debug;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.app.NotificationManagerCompat;

import com.apptentive.android.sdk.ApptentiveLog;

import java.io.File;
import java.util.ArrayList;

import static com.apptentive.android.sdk.debug.TroubleshootingNotificationBuilder.*;

public class LogBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		// Get the notification
		final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
		final int notificationId = intent.getIntExtra(NOTIFICATION_ID_KEY, 0);

		// Dismiss the notification
		notificationManager.cancel(notificationId);

		// Close the notification drawer
		Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		context.sendBroadcast(it);

		// Stop log monitor
		LogMonitor.stopSession(context);

		// handle action
		String action = intent.getAction();
		if (ACTION_SEND_LOGS.equals(action)) {
			ApptentiveLog.i("Send Report: User is sending Log Monitor email.");
			Intent email = new Intent(Intent.ACTION_SEND_MULTIPLE);
			email.setType("text/plain");
			email.putExtra(Intent.EXTRA_EMAIL, intent.getStringArrayExtra(EXTRA_EMAIL_RECIPIENTS));
			email.putExtra(Intent.EXTRA_SUBJECT, intent.getStringExtra(EXTRA_SUBJECT));
			email.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra(EXTRA_INFO));

			File[] files = (File[]) intent.getExtras().get(EXTRA_ATTACHMENTS);

			ArrayList<Uri> attachments = new ArrayList<>();
			for (File file : files) {
				if (file.exists()) {
					attachments.add(Uri.parse("content://" + ApptentiveAttachmentFileProvider.getAuthority(context) + "/" + file.getName()));
				}
			}
			email.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);

			Intent chooser = Intent.createChooser(email, "Choose an Email client:");
			chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

			context.startActivity(chooser);
		} else if (ACTION_ABORT.equals(action)) {
			ApptentiveLog.i("Discard: User exited log monitoring.");
		} else {
			ApptentiveLog.e("Unexpected action: %s", action);
		}
	}
}
