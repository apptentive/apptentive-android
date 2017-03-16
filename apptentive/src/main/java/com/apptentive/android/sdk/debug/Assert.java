package com.apptentive.android.sdk.debug;

import com.apptentive.android.sdk.util.StringUtils;

/**
 * A set of assertion methods useful for writing 'runtime' tests. These methods can be used directly:
 * Assert.assertEquals(...), however, they read better if they are referenced through static import:
 * <code>
 * import static org.junit.Assert.*;
 * ...
 * assertEquals(...);
 * </code>
 */
public class Assert {

	private static AssertImp imp;

	/**
	 * Asserts that condition is <code>true</code>
	 */
	public static void assertTrue(boolean condition) {
		if (imp != null && !condition) {
			imp.assertFailed("Expected 'true' but was 'false'");
		}
	}

	/**
	 * Asserts that condition is <code>true</code>
	 */
	public static void assertTrue(boolean condition, String message) {
		if (imp != null && !condition) {
			imp.assertFailed(message);
		}
	}

	/**
	 * Asserts that condition is <code>true</code>
	 */
	public static void assertTrue(boolean condition, String format, Object... args) {
		if (imp != null && !condition) {
			imp.assertFailed(StringUtils.format(format, args));
		}
	}

	/**
	 * Asserts that an object isn't null
	 */
	public static void assertNotNull(Object object) {
		if (imp != null && object == null) {
			imp.assertFailed("Not-null expected");
		}
	}

	/**
	 * Asserts that an object isn't null
	 */
	public static void assertNotNull(Object object, String message) {
		if (imp != null && object == null) {
			imp.assertFailed(message);
		}
	}

	/**
	 * Asserts that an object isn't null
	 */
	public static void assertNotNull(Object object, String format, Object... args) {
		if (imp != null && object == null) {
			imp.assertFailed(String.format(format, args));
		}
	}

	public static void setImp(AssertImp imp) {
		Assert.imp = imp;
	}
}
