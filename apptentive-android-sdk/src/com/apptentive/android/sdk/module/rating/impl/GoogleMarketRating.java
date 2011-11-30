/*
 * GoogleMarketRating.java
 *
 * Created by Dr. Cocktor on 2011-11-29.
 * Copyright 2011 MiKandi, LLC. All rights reserved.
 */

package com.apptentive.android.sdk.module.rating.impl;

import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.InsufficientRatingArgumentsException;

public class GoogleMarketRating implements IRatingProvider {

	@Override
	public void startRating(Context ctx, Map<String, String> args) throws InsufficientRatingArgumentsException {
		if(!args.containsKey("package")) {
			throw new InsufficientRatingArgumentsException("Missing required argument 'package'");
		}
		ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + args.get("package"))));
	}

}
