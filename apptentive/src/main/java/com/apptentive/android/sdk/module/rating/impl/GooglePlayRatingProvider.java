/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.rating.impl;

import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.InsufficientRatingArgumentsException;

public class GooglePlayRatingProvider implements IRatingProvider {
	public void startRating(Context context, Map<String, String> args) throws InsufficientRatingArgumentsException {
		if (!args.containsKey("package")) {
			String packageName = context.getPackageName();
			ApptentiveLog.w("Rating provider args did not contain package name. Adding default package: \"%s\"", packageName);
			args.put("package", packageName);
		}
		Uri uri = Uri.parse("market://details?id=" + args.get("package"));
		ApptentiveLog.i("Opening app store for rating with URI: \"%s\"", uri);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		int flag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? Intent.FLAG_ACTIVITY_NEW_DOCUMENT : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | flag | Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public String activityNotFoundMessage(Context ctx) {
		return ctx.getString(R.string.apptentive_rating_provider_no_google_play);
	}
}
