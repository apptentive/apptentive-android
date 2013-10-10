/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.apptentive.android.sdk.model.Sdk;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey
 */
public class SdkManager {

	public static Sdk storeSdkAndReturnDiff(Context context) {
		Sdk original = getStoredSdk(context);
		Sdk current = generateCurrentSdk(context);
		Sdk diff = diffSdk(original, current);
		if(diff != null) {
			storeSdk(context, current);
			return diff;
		}
		return null;
	}

	private static Sdk generateCurrentSdk(Context context) {
		Sdk sdk = new Sdk();

		// First, get all the information we can load from static resources.
		sdk.setVersion(Constants.APPTENTIVE_SDK_VERSION);
		sdk.setPlatform("Android");


		// Distribution and distribution version are optionally set in the manifest by the wrapping platform (trigger, etc.)
		Object distribution = Util.getPackageMetaDataSingleQuotedString(context, Constants.MANIFEST_KEY_SDK_DISTRIBUTION);
		if(distribution != null && distribution.toString().length() != 0) {
			sdk.setDistribution(distribution.toString());
		}
		Object distributionVersion = Util.getPackageMetaDataSingleQuotedString(context, Constants.MANIFEST_KEY_SDK_DISTRIBUTION_VERSION);
		if(distributionVersion != null && distributionVersion.toString().length() != 0) {
			sdk.setDistributionVersion(distributionVersion.toString());
		}

		return sdk;
	}

	private static Sdk getStoredSdk(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String sdkString = prefs.getString(Constants.PREF_KEY_SDK, null);
		try {
			return new Sdk(sdkString);
		} catch (Exception e) {
		}
		return null;
	}

	private static void storeSdk(Context context, Sdk sdk) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_SDK, sdk.toString()).commit();
	}

	private static Sdk diffSdk(Sdk older, Sdk newer) {
		if(older == null) {
			return newer;
		}

		Sdk ret = new Sdk();
		int baseEntries = ret.length();

		String version = chooseLatest(older.getVersion(), newer.getVersion());
		if (version != null) {
			ret.setVersion(version);
		}

		String programmingLanguage = chooseLatest(older.getProgrammingLanguage(), newer.getProgrammingLanguage());
		if (programmingLanguage != null) {
			ret.setProgrammingLanguage(programmingLanguage);
		}

		String authorName = chooseLatest(older.getAuthorName(), newer.getAuthorName());
		if (authorName != null) {
			ret.setAuthorName(authorName);
		}

		String authorEmail = chooseLatest(older.getAuthorEmail(), newer.getAuthorEmail());
		if (authorEmail != null) {
			ret.setAuthorEmail(authorEmail);
		}

		String platform = chooseLatest(older.getPlatform(), newer.getPlatform());
		if (platform != null) {
			ret.setPlatform(platform);
		}

		String distribution = chooseLatest(older.getDistribution(), newer.getDistribution());
		if (distribution != null) {
			ret.setDistribution(distribution);
		}

		String distributionVersion = chooseLatest(older.getDistributionVersion(), newer.getDistributionVersion());
		if (distributionVersion != null) {
			ret.setDistributionVersion(distributionVersion);
		}

		// If there were no differences, return null.
		if(ret.length() <= baseEntries) {
			return null;
		}
		return ret;
	}

	/**
	 * A convenience method.
	 *
	 * @return newer - if it is different from old. <p/>empty string - if there was an old value, but not a newer value. This clears the old value.<p/> null - if there is no difference.
	 */
	private static String chooseLatest(String old, String newer) {
		if (old == null || old.equals("")) {
			old = null;
		}
		if (newer == null || newer.equals("")) {
			newer = null;
		}

		// New value.
		if (old != null && newer != null && !old.equals(newer)) {
			return newer;
		}

		// Clear existing value.
		if (old != null && newer == null) {
			return "";
		}

		if (old == null && newer != null) {
			return newer;
		}

		// Do nothing.
		return null;
	}
}
