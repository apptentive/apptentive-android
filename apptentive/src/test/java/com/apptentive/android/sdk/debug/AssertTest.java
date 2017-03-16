/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.debug;

import com.apptentive.android.sdk.TestCaseBase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AssertTest extends TestCaseBase implements AssertImp {

	//region Setup

	@Before
	public void setUp() {
		AssertEx.setImp(this);
	}

	@After
	public void tearDown() {
		AssertEx.setImp(null);
	}

	//endregion

	@Test
	public void testAssertTrue() throws Exception {
		AssertEx.assertTrue(true);
		AssertEx.assertTrue(false);
		AssertEx.assertTrue(true, "");
		AssertEx.assertTrue(false, "assertTrue(boolean,String)");
		AssertEx.assertTrue(true, "", new Object());
		AssertEx.assertTrue(false, "assertTrue(boolean,String,Object...)");

		assertResult(
			"Expected 'true' but was 'false'",
			"assertTrue(boolean,String)",
			"assertTrue(boolean,String,Object...)"
		);
	}

	@Test
	public void testAssertNotNull() throws Exception {
		AssertEx.assertNotNull(new Object());
		AssertEx.assertNotNull(null);
		AssertEx.assertNotNull(new Object(), "");
		AssertEx.assertNotNull(null, "assertNotNull(Object,String)");
		AssertEx.assertNotNull(new Object(), "", new Object());
		AssertEx.assertNotNull(null, "assertNotNull(Object,String,Object...)");

		assertResult(
			"Not-null expected",
			"assertNotNull(Object,String)",
			"assertNotNull(Object,String,Object...)"
		);
	}

	@Override
	public void assertFailed(String message) {
		addResult(message);
	}

	private static class AssertEx extends com.apptentive.android.sdk.debug.Assert
	{
	}
}