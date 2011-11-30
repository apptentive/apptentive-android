/*
 * GlobalInfo.java
 *
 * Created by Sky Kelsey on 2011-11-05.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 * Edited by Dr. Cocktor on 2011-11-29.
 * 		+ Updated to support pluggable ratings by including
 * 		a 'ratingArgs' field.
 * 		+ Fixed incorrect name in file header
 * 		+ Changes Copyright 2011 MiKandi, LLC. All right reserved.
 */

package com.apptentive.android.sdk.model;

import java.util.Map;

import com.apptentive.android.sdk.module.rating.IRatingProvider;

public class GlobalInfo {
	public static final String APPTENTIVE_API_VERSION = "1.0";

	public static String manufacturer;
	public static String model;
	public static String version;
	public static String carrier;

	public static String androidId;
	public static String appDisplayName;
	public static String appPackage;
	public static String apiKey;

	public static String userEmail;
	
	public static Class<? extends IRatingProvider> ratingProvider;
	public static Map<String, String> ratingArgs;
}
