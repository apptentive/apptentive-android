package com.apptentive.android.sdk.module.rating.impl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.InsufficientRatingArgumentsException;

import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class AmazonAppstoreRatingProvider implements IRatingProvider {
	public void startRating(Context context, Map<String, String> args) throws InsufficientRatingArgumentsException {
		if (!args.containsKey("package")) {
			throw new InsufficientRatingArgumentsException("Missing required argument 'package'");
		}
		context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("amzn://apps/android?p=" + args.get("package"))));
	}

	public String activityNotFoundMessage(Context context) {
		return context.getString(R.string.apptentive_rating_provider_no_amazon_appstore);
	}
}
