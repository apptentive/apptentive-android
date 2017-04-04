/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.notifications.ApptentiveNotification;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationObserver;
import com.apptentive.android.sdk.util.Destroyable;

import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_APP_ENTER_BACKGROUND;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_APP_ENTER_FOREGROUND;

public class PayloadSender implements ApptentiveNotificationObserver, Destroyable {
	private final PayloadDataSource dataSource;

	public PayloadSender(PayloadDataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException("Data source is null");
		}
		this.dataSource = dataSource;

		ApptentiveNotificationCenter.defaultCenter().addObserver(NOTIFICATION_APP_ENTER_BACKGROUND, this);
		ApptentiveNotificationCenter.defaultCenter().addObserver(NOTIFICATION_APP_ENTER_FOREGROUND, this);
	}

	//region Background/Foreground

	private void onAppEnterBackground() {
	}

	private void onAppEnterForeground() {
	}

	//endregion

	//region Destroyable

	@Override
	public void destroy() {
		ApptentiveNotificationCenter.defaultCenter().removeObserver(this);
	}

	//endregion

	//region ApptentiveNotificationObserver

	@Override
	public void onReceiveNotification(ApptentiveNotification notification) {
		if (notification.hasName(NOTIFICATION_APP_ENTER_BACKGROUND)) {
			onAppEnterBackground();
		} else if (notification.hasName(NOTIFICATION_APP_ENTER_FOREGROUND)) {
			onAppEnterForeground();
		}
	}

	//endregion
}
