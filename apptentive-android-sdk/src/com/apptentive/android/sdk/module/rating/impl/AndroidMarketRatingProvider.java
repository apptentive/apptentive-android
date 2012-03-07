/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.rating.impl;

import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.InsufficientRatingArgumentsException;

public class AndroidMarketRatingProvider implements IRatingProvider {
	public void startRating(Context context, Map<String, String> args) throws InsufficientRatingArgumentsException {
		if (!args.containsKey("package")) {
			throw new InsufficientRatingArgumentsException("Missing required argument 'package'");
		}
		context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + args.get("package"))));
	}

	public String activityNotFoundMessage(Context ctx) {
		return ctx.getString(R.string.apptentive_rating_provider_no_android_market);
	}
}
