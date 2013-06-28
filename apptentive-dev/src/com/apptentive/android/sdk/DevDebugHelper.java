/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.content.Context;
import com.apptentive.android.sdk.module.messagecenter.MessageCenterHelper;

/**
 * All methods in this class are here for dev/debug purposes only. This class allows us to access package private methods
 * that a normal host app will not need to access.
 * @author Sky Kelsey
 */
public class DevDebugHelper {
	public static void logDay(Context context) {
		RatingModule.getInstance().logDay(context);
	}

	public static void resetRatingFlow(Context context) {
		RatingModule.getInstance().reset(context);
	}

	public static void forceShowEnjoymentDialog(Activity activity) {
		RatingModule.getInstance().forceShowEnjoymentDialog(activity);
	}

	public static void showRatingDialog(Activity activity) {
		RatingModule.getInstance().forceShowRatingDialog(activity);
	}

	public static void forceShowIntroDialog(Activity activity) {
		MessageCenterHelper.forceShowIntroDialog(activity);
	}
}
