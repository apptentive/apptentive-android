/*
 * MiKandiMarketRating.java
 *
 * Created by Dr. Cocktor on 2011-11-29.
 * Copyright 2011 MiKandi, LLC. All rights reserved.
 */

package com.apptentive.android.sdk.module.rating.impl;

import java.util.Map;

import android.content.Context;
import android.widget.Toast;

import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.InsufficientRatingArgumentsException;

/**
 * Implements ratings using the MiKandi market. At the moment this is just a
 * placeholder while changes are made to mikandi to allow a direct inward link
 * to a ratings dialog.
 */
public class MiKandiMarketRating implements IRatingProvider {

	@Override
	public void startRating(Context ctx, Map<String, String> args) throws InsufficientRatingArgumentsException {
		Toast.makeText(ctx, "MiKandi Ratings are not yet availale. Please visit the MiKandi Market", Toast.LENGTH_LONG).show();
	}

}
