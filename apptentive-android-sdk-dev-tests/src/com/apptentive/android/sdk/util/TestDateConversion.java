/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.test.AndroidTestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class TestDateConversion extends AndroidTestCase {

	public void testIso8601Conversion() {
		Map<String, Long> values = new HashMap<String, Long>();
		values.put("2012-10-16 11:53:45.849-0700", 1350413625849l);
		values.put("2012-10-16 11:53:45-0700",     1350413625000l);
		values.put("2012-08-08 01:01:01.000-700",  1344412861000l);
		values.put("2012-08-08 01:01:01.001-700",  1344412861001l);
		values.put("2012-08-08 01:01:59.999-700",  1344412919999l);
		values.put("2012-08-08 01:01:00.000-700",  1344412860000l);
		values.put("2012-00-00 00:00:00.000-700",  1322636400000l);
		values.put("2012-08-08 01:01:01-0700",     1344412861000l);
		values.put("2012-08-08 01:01:01-700",      1344412861000l);
		values.put("2012-08-08 01:01:01-7:00",     1344412861000l);
		for (String date : values.keySet()) {
			Long result = Util.parseIso8601Date(date).getTime();
			assertEquals(result, values.get(date));
		}
	}
}
