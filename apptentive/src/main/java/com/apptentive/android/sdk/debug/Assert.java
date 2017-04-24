package com.apptentive.android.sdk.debug;

import android.os.Looper;

import com.apptentive.android.sdk.util.ObjectUtils;
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

	//region Booleans

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
	 * Asserts that condition is <code>false</code>
	 */
	public static void assertFalse(boolean condition) {
		if (imp != null && condition) {
			imp.assertFailed("Expected 'false' but was 'true'");
		}
	}

	/**
	 * Asserts that condition is <code>false</code>
	 */
	public static void assertFalse(boolean condition, String message) {
		if (imp != null && condition) {
			imp.assertFailed(message);
		}
	}

	/**
	 * Asserts that condition is <code>false</code>
	 */
	public static void assertFalse(boolean condition, String format, Object... args) {
		if (imp != null && condition) {
			imp.assertFailed(StringUtils.format(format, args));
		}
	}

	//endregion

	//region Nullability

	/** Helper function for getting non-null references */
	public static <T> T notNull(T reference) {
		assertNotNull(reference);
		return reference;
	}

	/**
	 * Asserts that an object isn't <code>null</code>
	 */
	public static void assertNotNull(Object object) {
		if (imp != null && object == null) {
			imp.assertFailed("Not-null expected");
		}
	}

	/**
	 * Asserts that an object isn't <code>null</code>
	 */
	public static void assertNotNull(Object object, String message) {
		if (imp != null && object == null) {
			imp.assertFailed(message);
		}
	}

	/**
	 * Asserts that an object isn't <code>null</code>
	 */
	public static void assertNotNull(Object object, String format, Object... args) {
		if (imp != null && object == null) {
			imp.assertFailed(String.format(format, args));
		}
	}

	/**
	 * Asserts that an object is <code>null</code>
	 */
	public static void assertNull(Object object) {
		if (imp != null && object != null) {
			imp.assertFailed(StringUtils.format("Expected 'null' but was '%s'", object));
		}
	}

	/**
	 * Asserts that an object is <code>null</code>
	 */
	public static void assertNull(Object object, String message) {
		if (imp != null && object != null) {
			imp.assertFailed(message);
		}
	}

	/**
	 * Asserts that an object is <code>null</code>
	 */
	public static void assertNull(Object object, String format, Object... args) {
		if (imp != null && object != null) {
			imp.assertFailed(String.format(format, args));
		}
	}

	//endregion

	//region Equality

	public static void assertEquals(Object expected, Object actual) {
		if (imp != null && !ObjectUtils.equal(expected, actual)) {
			imp.assertFailed(StringUtils.format("Expected '%s' but was '%s'", expected, actual));
		}
	}

	//endregion

	//region Threading

	/**
	 * Asserts that code executes on the main thread.
	 */
	public static void assertMainThread() {
		if (imp != null && Looper.myLooper() != Looper.getMainLooper()) {
			imp.assertFailed(StringUtils.format("Expected 'main' thread but was '%s'", Thread.currentThread().getName()));
		}
	}

	/**
	 * Asserts that code executes on the main thread.
	 */
	public static void assertBackgroundThread() {
		if (imp != null && Looper.myLooper() == Looper.getMainLooper()) {
			imp.assertFailed("Expected background thread but was 'main'");
		}
	}

	//endregion

	//region Failure

	/**
	 * General failure with a message
	 */
	public static void assertFail(String message) {
		if (imp != null) {
			imp.assertFailed(message);
		}
	}

	//endregion

	public static void setImp(AssertImp imp) {
		Assert.imp = imp;
	}
}
