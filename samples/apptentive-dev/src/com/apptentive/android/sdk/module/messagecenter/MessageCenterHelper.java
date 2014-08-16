/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;

import android.app.Activity;

/**
 * @author Sky Kelsey
 */
public class MessageCenterHelper {
	public static void forceShowIntroDialog(Activity activity) {
		ApptentiveMessageCenter.showIntroDialog(activity, ApptentiveMessageCenter.Trigger.message_center, false);
	}
}
