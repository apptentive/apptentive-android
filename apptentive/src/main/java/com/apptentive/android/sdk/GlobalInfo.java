/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.content.Context;
import android.content.SharedPreferences;

import com.apptentive.android.sdk.util.Constants;

/**
 * @author Sky Kelsey
 */
public class GlobalInfo {

	public static String version;

	public static String appDisplayName;

	public static void reset(Context appContext) {
		if (appContext == null) {
			return;
		}
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit()
				.remove(Constants.PREF_KEY_PERSON_ID)
				.remove(Constants.PREF_KEY_CONVERSATION_TOKEN)
				.apply();
	}
}