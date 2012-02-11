/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.rating.impl;

import java.util.Map;

import android.content.Context;
import android.widget.Toast;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.InsufficientRatingArgumentsException;

/**
 * Implements ratings using the MiKandi market. At the moment this is just a
 * placeholder while changes are made to mikandi to allow a direct inward link
 * to a ratings dialog.
 */
public class MiKandiMarketRatingProvider implements IRatingProvider {
	public void startRating(Context context, Map<String, String> args) throws InsufficientRatingArgumentsException {
		Toast.makeText(context, "MiKandi Ratings are not yet available. Please visit the MiKandi Market", Toast.LENGTH_LONG).show();
	}

	public String activityNotFoundMessage(Context ctx) {
		return ctx.getString(R.string.apptentive_rating_provider_no_mikandi);
	}
}
