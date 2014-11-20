/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.misc;

import android.test.AndroidTestCase;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey
 */
public class RandomTests extends AndroidTestCase {

	public void testGetMajorOsVersion() {
		Log.e("Running test: testGetMajorOsVersion()\n\n");
		Integer osVersion = Util.getMajorOsVersion();
		Log.e("OS version: %d", osVersion);
		assertNotNull(osVersion);
	}
}
