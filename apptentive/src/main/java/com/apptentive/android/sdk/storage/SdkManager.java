/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.SharedPreferences;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.model.Sdk;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

public class SdkManager {

	public static Sdk storeSdkAndReturnIt() {
		Sdk current = generateCurrentSdk();
		storeSdk(current);
		return current;
	}

	public static Sdk generateCurrentSdk() {
		Sdk sdk = new Sdk();

		// First, get all the information we can load from static resources.
		sdk.setVersion(Constants.APPTENTIVE_SDK_VERSION);
		sdk.setPlatform("Android");

		// Distribution and distribution version are optionally set in the manifest by the wrapping platform (Cordova, mParticle, etc.)
		Object distribution = Util.getPackageMetaDataSingleQuotedString(ApptentiveInternal.getInstance().getApplicationContext(), Constants.MANIFEST_KEY_SDK_DISTRIBUTION);
		if (distribution != null && distribution.toString().length() != 0) {
			sdk.setDistribution(distribution.toString());
		}
		Object distributionVersion = Util.getPackageMetaDataSingleQuotedString(ApptentiveInternal.getInstance().getApplicationContext(), Constants.MANIFEST_KEY_SDK_DISTRIBUTION_VERSION);
		if (distributionVersion != null && distributionVersion.toString().length() != 0) {
			sdk.setDistributionVersion(distributionVersion.toString());
		}
		return sdk;
	}

	public static void storeSdk(Sdk sdk) {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		prefs.edit().putString(Constants.PREF_KEY_SDK, sdk.toString()).apply();
	}
}
