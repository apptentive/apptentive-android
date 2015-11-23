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


	public static boolean initialized = false;
	public static boolean isAppDebuggable = false;

	public static String version;

	public static String androidId;
	public static String appDisplayName;
	public static String appPackage;
	public static String apiKey = null;

	public static void reset(Context appContext) {
		if (appContext == null) {
			return;
		}
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit()
				.remove(Constants.PREF_KEY_PERSON_ID)
				.remove(Constants.PREF_KEY_CONVERSATION_TOKEN)
				.apply();
		initialized = false;
	}

	public static String getPersonId(Context appContext) {
		if (appContext == null) {
			return null;
		}
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		return prefs.getString(Constants.PREF_KEY_PERSON_ID, null);
	}

	public static void setPersonId(Context appContext, String newId) {
		if (appContext == null) {
			return;
		}
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_PERSON_ID, newId).commit();
	}

	public static String getConversationToken(Context appContext) {
		if (appContext == null) {
			return null;
		}
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		return prefs.getString(Constants.PREF_KEY_CONVERSATION_TOKEN, null);
	}

	public static void setConversationToken(Context appContext, String newToken) {
		if (appContext == null) {
			return;
		}
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_CONVERSATION_TOKEN, newToken).commit();
	}

	public static void setConversationId(Context appContext, String newConversationId) {
		if (appContext == null) {
			return;
		}
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_CONVERSATION_ID, newConversationId).commit();
	}
}