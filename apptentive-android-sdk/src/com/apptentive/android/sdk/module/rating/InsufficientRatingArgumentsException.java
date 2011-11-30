/*
 * InsufficientRatingArgumentsException.java
 *
 * Created by Dr. Cocktor on 2011-11-29.
 * Copyright 2011 MiKandi, LLC. All rights reserved.
 */


package com.apptentive.android.sdk.module.rating;

/**
 * Indicates that a implementation of {@link IRatingProvider} was not
 * provided necessary and/or sufficient arguments to successfully kick
 * off a rating workflow.
 */
public class InsufficientRatingArgumentsException extends Exception {
	public InsufficientRatingArgumentsException(String message) {
		super(message);
	}
	private static final long serialVersionUID = -4592353045389664388L;
}
