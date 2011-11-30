/*
 * IRatingProvider.java
 *
 * Created by Dr. Cocktor on 2011-11-29.
 * Copyright 2011 MiKandi, LLC. All rights reserved.
 */

package com.apptentive.android.sdk.module.rating;

import java.util.Map;

import android.content.Context;

/**
 * Provides a common interface for ratings providers to
 * allow Apptentive to work with the multiple markets available
 * on the Android platform.
 */
public interface IRatingProvider {
	/**
	 * Starts the rating process. Implementations should gracefully
	 * handle cases where not all required arguments are provided.
	 * @param args A list of keys and values which may have been
	 * provided at app initialization to allow the ratings provider
	 * access to additional information. The hash will, at a minimum,
	 * contain the 'name' of the app as passed to apptentive and the
	 * 'package' identifier of the app.
	 * @param ctx An Android {@link Context} used to launch the rating
	 * or provide dialogs or notifications.
	 * @throws InsufficientRatingArgumentsException
	 */
	public void startRating(Context ctx, Map<String,String> args) throws InsufficientRatingArgumentsException;
	
	/**
	 * Called if the startRating process does not successfully finish launching an activity.
	 * @param The current context
	 * @return The error message to display to users.
	 */
	public String activityNotFoundMessage(Context ctx);
}
