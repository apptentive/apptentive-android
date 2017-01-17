package com.apptentive.android.sdk.debug;

import java.util.Collection;

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

	/**
	 * Asserts that an object isn't null
	 */
	public static void assertNotNull(Object object) {
		// FIXME: implement me
	}

	/**
	 * Asserts that an object isn't null
	 */
	public static void assertNotNull(Object object, String message) {
		// FIXME: implement me
	}

	/**
	 * Asserts that an object isn't null
	 */
	public static void assertNotNull(Object object, String format, Object... args) {
		// FIXME: implement me
	}

	/**
	 * Asserts that collection contains an object
	 */
	public static void assertContains(Collection<?> collection, Object object) {
		// FIXME: implement me
	}

	/**
	 * Asserts that collection contains an object
	 */
	public static void assertContains(Collection<?> collection, Object object, String message) {
		// FIXME: implement me
	}

	/**
	 * Asserts that collection contains an object
	 */
	public static void assertContains(Collection<?> collection, Object object, String format, Object... args) {
		// FIXME: implement me
	}

	/**
	 * Asserts that collection doesn't contain an object
	 */
	public static void assertNotContains(Collection<?> collection, Object object) {
		// FIXME: implement me
	}

	/**
	 * Asserts that collection doesn't contain an object
	 */
	public static void assertNotContains(Collection<?> collection, Object object, String message) {
		// FIXME: implement me
	}

	/**
	 * Asserts that collection doesn't contain an object
	 */
	public static void assertNotContains(Collection<?> collection, Object object, String format, Object... args) {
		// FIXME: implement me
	}

	/** Asserts that code is executed on the main thread */
	public static void assertMainThread() {
	}

	/** Asserts that code is not executed on the main thread */
	public static void assertNotMainThread() {
	}
}
