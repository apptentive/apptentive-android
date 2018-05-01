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
	public void setUp() throws Exception {
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
	public void testAssertFalse() throws Exception {
		AssertEx.assertFalse(false);
		AssertEx.assertFalse(true);
		AssertEx.assertFalse(false, "");
		AssertEx.assertFalse(true, "assertFalse(boolean,String)");
		AssertEx.assertFalse(false, "", new Object());
		AssertEx.assertFalse(true, "assertFalse(boolean,String,Object...)");

		assertResult(
			"Expected 'false' but was 'true'",
			"assertFalse(boolean,String)",
			"assertFalse(boolean,String,Object...)"
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

	@Test
	public void testAssertNull() throws Exception {
		AssertEx.assertNull(null);
		AssertEx.assertNull("foo");
		AssertEx.assertNull(null);
		AssertEx.assertNull("foo", "assertNull(Object,String)");
		AssertEx.assertNull(null, "", new Object());
		AssertEx.assertNull("foo", "assertNull(Object,String,Object...)");

		assertResult(
			"Expected 'null' but was 'foo'",
			"assertNull(Object,String)",
			"assertNull(Object,String,Object...)"
		);
	}

	@Test
	public void testAssertEquals() throws Exception {
		AssertEx.assertEquals("foo", "foo");
		AssertEx.assertEquals("foo", "bar");
		assertResult("Expected 'foo' but was 'bar'");
	}

	@Override
	public void assertFailed(String message) {
		addResult(message);
	}

	private static class AssertEx extends com.apptentive.android.sdk.debug.Assert
	{
	}
}