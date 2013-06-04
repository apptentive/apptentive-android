/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import com.apptentive.android.sdk.module.messagecenter.MessageCenterHelper;

/**
 * All methods in this class are here for dev/debug purposes only. This class allows us to access package private methods
 * that a normal host app will not need to access.
 * @author Sky Kelsey
 */
public class DevDebugHelper {
	public static void resetRatingFlow() {
		RatingModule.getInstance().reset();
	}

	public static void forceShowEnjoymentDialog(Activity activity) {
		RatingModule.getInstance().forceShowEnjoymentDialog(activity);
	}

	public static void showRatingDialog(Activity activity) {
		RatingModule.getInstance().forceShowRatingDialog(activity);
	}

	public static void forceShowFeedbackDialog(Activity activity) {
		MessageCenterHelper.forceShowFeedbackDialog(activity);
	}
}
