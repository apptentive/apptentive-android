/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;
import android.content.res.Resources;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.SdkPayload;
import com.apptentive.android.sdk.util.Constants;

public class SdkManager {

	public static Sdk generateCurrentSdk(Context context) {
		Sdk sdk = new Sdk();

		// First, get all the information we can load from static resources.
		sdk.setVersion(Constants.getApptentiveSdkVersion());
		sdk.setPlatform("Android");

		// Distribution and distribution version are optionally set in the manifest by the wrapping platform (Cordova, mParticle, etc.)
		Resources resources = context.getResources();
		sdk.setDistribution(resources.getString(R.string.apptentive_distribution));
		sdk.setDistributionVersion(resources.getString(R.string.apptentive_distribution_version));
		ApptentiveLog.v("SDK: %s:%s", sdk.getDistribution(), sdk.getDistributionVersion());
		return sdk;
	}

	public static SdkPayload getPayload(Sdk sdk) {
		SdkPayload ret = new SdkPayload();
		if (sdk == null) {
			return ret;
		}

		ret.setAuthorEmail(sdk.getAuthorEmail());
		ret.setAuthorName(sdk.getAuthorName());
		ret.setDistribution(sdk.getDistribution());
		ret.setDistributionVersion(sdk.getDistributionVersion());
		ret.setPlatform(sdk.getPlatform());
		ret.setProgrammingLanguage(sdk.getProgrammingLanguage());
		ret.setVersion(sdk.getVersion());
		return ret;
	}
}
