/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

/**
 * @author Sky Kelsey
 */
public class GlobalInfo {
	// Don't ever use "1.0". I prematurely incremented to 1.0, so we should skip over it.
	public static final String APPTENTIVE_API_VERSION = "0.5.1";

	public static boolean initialized = false;
	public static boolean isAppDebuggable = false;

	public static String version;
	public static String carrier;
	public static String currentCarrier;
	public static int    networkType;

	public static String androidId;
	public static String appDisplayName;
	public static String appPackage;
	public static String apiKey = null;

	public static String userEmail;
}
