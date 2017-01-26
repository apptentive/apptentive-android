//
//  StringUtils.java
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

package com.apptentive.android.sdk.util;

import java.util.List;

/**
 * A collection of useful string-related functions
 */
public final class StringUtils {

	/**
	 * Safe <code>String.format</code>
	 */
	public static String format(String format, Object... args) {
		if (format != null && args != null && args.length > 0) {
			try {
				return String.format(format, args);
			} catch (Exception e) {
				android.util.Log.e("Apptentive", "Error while formatting String: " + e.getMessage()); // FIXME: better system logging
			}
		}

		return format;
	}

	/**
	 * Safe <code>Object.toString()</code>
	 */
	public static String toString(Object value) {
		return value != null ? value.toString() : "null";
	}

	/**
	 * Constructs and returns a string object that is the result of interposing a separator between the elements of the array
	 */
	public static <T> String join(T[] array) {
		return join(array, ",");
	}

	/**
	 * Constructs and returns a string object that is the result of interposing a separator between the elements of the array
	 */
	public static <T> String join(T[] array, String separator) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < array.length; ++i) {
			builder.append(array[i]);
			if (i < array.length - 1) builder.append(separator);
		}
		return builder.toString();
	}

	/**
	 * Constructs and returns a string object that is the result of interposing a separator between the elements of the list
	 */
	public static <T> String join(List<T> list) {
		return join(list, ",");
	}

	/**
	 * Constructs and returns a string object that is the result of interposing a separator between the elements of the list
	 */
	public static <T> String join(List<T> list, String separator) {
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (T t : list) {
			builder.append(t);
			if (++i < list.size()) builder.append(separator);
		}
		return builder.toString();
	}
}
