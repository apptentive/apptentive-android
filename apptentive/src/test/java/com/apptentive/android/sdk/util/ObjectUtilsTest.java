package com.apptentive.android.sdk.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class ObjectUtilsTest {

	@Test
	public void testIsNull() {
		byte[] array = null;
		assertTrue(ObjectUtils.isNullOrEmpty(array));
	}

	@Test
	public void testIsEmpty() {
		byte[] array = new byte[0];
		assertTrue(ObjectUtils.isNullOrEmpty(array));
	}

	@Test
	public void testNotEmpty() {
		byte[] array = {1, 2, 3};
		assertFalse(ObjectUtils.isNullOrEmpty(array));
	}
}