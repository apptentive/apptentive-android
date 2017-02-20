//
//  Assert.java
//
//  Lunar Unity Mobile Console
//  https://github.com/SpaceMadness/lunar-unity-console
//
//  Copyright 2017 Alex Lementuev, SpaceMadness.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

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
	 * Asserts that condition is <code>true</code>
	 */
	public static void assertTrue(boolean condition) {
		// FIXME: implement me
	}

	/**
	 * Asserts that condition is <code>true</code>
	 */
	public static void assertTrue(boolean condition, String message) {
		// FIXME: implement me
	}

	/**
	 * Asserts that condition is <code>true</code>
	 */
	public static void assertTrue(boolean condition, String format, Object... args) {
		// FIXME: implement me
	}

	/**
	 * Asserts that condition is <code>false</code>
	 */
	public static void assertFalse(boolean condition) {
		// FIXME: implement me
	}

	/**
	 * Asserts that condition is <code>false</code>
	 */
	public static void assertFalse(boolean condition, String message) {
		// FIXME: implement me
	}

	/**
	 * Asserts that condition is <code>false</code>
	 */
	public static void assertFalse(boolean condition, String format, Object... args) {
		// FIXME: implement me
	}

	/**
	 * Asserts that an object is null
	 */
	public static void assertNull(Object object) {
		// FIXME: implement me
	}

	/**
	 * Asserts that an object is null
	 */
	public static void assertNull(Object object, String message) {
		// FIXME: implement me
	}

	/**
	 * Asserts that an object is null
	 */
	public static void assertNull(Object object, String format, Object... args) {
		// FIXME: implement me
	}

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

	/** Asserts that <code>executed</code> is not equal to <code>actual</code> */
	public static void assertNotEquals(int expected, int actual) {
		// FIXME: implement me
	}

	/** Asserts that <code>executed</code> is not equal to <code>actual</code> */
	public static void assertNotEquals(int expected, int actual, String message) {
		// FIXME: implement me
	}

	/** Asserts that <code>executed</code> is not equal to <code>actual</code> */
	public static void assertNotEquals(int expected, int actual, String format, Object... args) {
		// FIXME: implement me
	}

	/**
	 * Asserts that collection isRegistered an object
	 */
	public static void assertContains(Collection<?> collection, Object object) {
		// FIXME: implement me
	}

	/**
	 * Asserts that collection isRegistered an object
	 */
	public static void assertContains(Collection<?> collection, Object object, String message) {
		// FIXME: implement me
	}

	/**
	 * Asserts that collection isRegistered an object
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
