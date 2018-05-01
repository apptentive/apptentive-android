/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.notification;

import android.content.Context;
import android.content.Intent;

public interface InteractionNotificationAdapter {
	void handleInteractionNotificationAction(Context context, String channelId, Intent intent);
}
