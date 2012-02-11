/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

/**
 * @author Sky Kelsey
 */
public class GlobalInfo {
	public static final String APPTENTIVE_API_VERSION = "1.0";

	public static String version;
	public static String carrier;
	public static String currentCarrier;
	public static int    networkType;

	public static String androidId;
	public static String appDisplayName;
	public static String appPackage;
	public static String apiKey;

	public static String userEmail;
	
	public static Class<? extends IRatingProvider> ratingProvider;
	public static Map<String, String> ratingArgs;
}
