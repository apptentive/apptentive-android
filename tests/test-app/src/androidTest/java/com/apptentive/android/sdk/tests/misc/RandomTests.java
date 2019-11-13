/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.misc;

import androidx.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.util.Util;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class RandomTests {

	@Test
	public void getMajorOsVersion() {
		Integer osVersion = Util.getMajorOsVersion();
		assertNotNull(osVersion);
	}

	@Test
	public void parseWebColorAsAndroidColor() {
		Map<String, Integer> data = new HashMap<String, Integer>() {{
			put("#AABBCCDD", 0xDDAABBCC);
			put("#FFFFFFFF", 0xFFFFFFFF);
			put("#00000000", 0x00000000);
			put("#000000FF", 0xFF000000);
			put("#FF000000", 0x00FF0000);
			put("#12345678", 0x78123456);
			put("#34567812", 0x12345678);
		}};
		Set<String> inputs = data.keySet();
		for (String input : inputs) {
			Integer expected = data.get(input);
			Integer actual = Util.parseWebColorAsAndroidColor(input);
			assertEquals(String.format("Conversion failed for %s: Expected 0x%08X, but got 0x%08X -", input, expected, actual), expected, actual);
		}
	}
}
