/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.debug;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.apptentive.android.sdk.ApptentiveHelper.checkConversationQueue;
import static com.apptentive.android.sdk.ApptentiveLog.Level.VERBOSE;
import static com.apptentive.android.sdk.ApptentiveLogTag.TROUBLESHOOT;
import static com.apptentive.android.sdk.debug.Assert.assertNotNull;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;
import static com.apptentive.android.sdk.util.Constants.LOG_FILE_EXT;
import static com.apptentive.android.sdk.util.Constants.LOG_FILE_PREFIX;

class LogMonitorSession {
	// TODO: Replace with a better unique number
	private static final int NOTIFICATION_ID = 1;

	/**
	 * Email recipients for the log email
	 */
	String[] emailRecipients = {"support@apptentive.com"};

	/**
	 * True if configuration was restored from the persistent storage
	 */
	boolean restored;

	private ApptentiveLog.Level oldLogLevel;

	//region Lifecycle

	void start(final Context context) {
		checkConversationQueue();

		ApptentiveLog.i(TROUBLESHOOT, "Overriding log level: " + VERBOSE);
		oldLogLevel = ApptentiveLog.getLogLevel();
		ApptentiveLog.overrideLogLevel(VERBOSE);

		// show debug notification
		showDebugNotification(context);
	}

	public void stop() {
		assertNotNull(oldLogLevel);
		if (oldLogLevel != null) {
			ApptentiveLog.overrideLogLevel(oldLogLevel);
		}
	}

	private void showDebugNotification(final Context context) {
		final String subject = getSubject(context);
		final File[] attachments = listAttachments(context);

		DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				Notification notification = TroubleshootingNotificationBuilder.buildNotification(context, subject, getSystemInfo(context), attachments, emailRecipients);
				Assert.assertNotNull(notification, "Failed to create troubleshooting notification");
				if (notificationManager != null) {
					notificationManager.notify(NOTIFICATION_ID, notification);
				}
			}
		});
	}

	private String getSubject(Context context) {
		String subject = String.format("%s (Android)", context.getPackageName());
		try {
			ApplicationInfo ai = context.getApplicationInfo();
			subject = String.format("%s (Android)", ai.loadLabel(context.getPackageManager()).toString());
		} catch (Exception e) {
			ApptentiveLog.e(TROUBLESHOOT, e, "Unable to load troubleshooting email status line");
			logException(e);
		}
		return subject;
	}

	private String getSystemInfo(Context context) {
		String versionName = "";
		int versionCode = -1;
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			// TODO: list activities, permissions, etc
			versionName = packageInfo.versionName;
			versionCode = packageInfo.versionCode;
		} catch (Exception e) {
			ApptentiveLog.e(e, "Unable to get app version info");
			logException(e);
		}

		Object[] info = {
				"App Package Name", context.getPackageName(),
				"App Version Name", versionName,
				"App Version Code", versionCode,
				"Apptentive SDK", com.apptentive.android.sdk.util.Constants.getApptentiveSdkVersion(),
				"Device Model", Build.MODEL,
				"Android OS Version", Build.VERSION.RELEASE,
				"Android OS API Level", Build.VERSION.SDK_INT,
				"Locale", Locale.getDefault().getDisplayName()
		};

		StringBuilder result = new StringBuilder();
		result.append("This email may contain sensitive content. Please review before sending.\n\n");
		for (int i = 0; i < info.length; i += 2) {
			if (result.length() > 0) {
				result.append("\n");
			}
			result.append(info[i]);
			result.append(": ");
			result.append(info[i + 1]);
		}
		return result.toString();
	}

	private static File[] listAttachments(Context context) {
		List<File> attachments = new ArrayList<>();

		// manifest
		File manifestFile = new File(ApptentiveLog.getLogsDirectory(context), Constants.FILE_APPTENTIVE_ENGAGEMENT_MANIFEST);
		attachments.add(manifestFile);

		// logs
		File logsDirectory = ApptentiveLog.getLogsDirectory(context);
		File[] logFiles = logsDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(LOG_FILE_EXT) && name.startsWith(LOG_FILE_PREFIX);
			}
		});
		if (logFiles != null && logFiles.length > 0) {
			attachments.addAll(Arrays.asList(logFiles));
		}

		return attachments.toArray(new File[attachments.size()]);
	}

	//endregion

	@Override
	public String toString() {
		return StringUtils.format("recipients=%s restored=%s",
				Arrays.toString(emailRecipients),
				Boolean.toString(restored));
	}
}
