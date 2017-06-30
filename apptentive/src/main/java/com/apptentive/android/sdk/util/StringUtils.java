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

import com.apptentive.android.sdk.ApptentiveLog;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

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
	 * Transforms dictionary to string
	 */
	public static String toString(Map<?, ?> map) {
		if (map == null) return null;

		StringBuilder result = new StringBuilder();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (result.length() > 0) result.append(", ");
			result.append("'");
			result.append(entry.getKey());
			result.append("':'");
			result.append(entry.getValue());
			result.append("'");
		}

		return result.toString();
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

	/**
	 * Create URL encoded params string from the map of key-value pairs
	 *
	 * @throws IllegalArgumentException if map, any key or value appears to be null
	 */
	public static String createQueryString(Map<String, Object> params) { // FIXME: unit tests (DO NOT ACCEPT PULL REQUEST IF YOU SEE THIS COMMENT)
		if (params == null) {
			throw new IllegalArgumentException("Params are null");
		}

		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, Object> e : params.entrySet()) {
			String key = e.getKey();
			if (key == null) {
				throw new IllegalArgumentException("key is null");
			}

			Object valueObj = e.getValue();
			if (valueObj == null) {
				throw new IllegalArgumentException("value is null for key '" + key + "'");
			}

			String value = valueObj.toString();

			@SuppressWarnings("deprecation")
			String encodedKey = URLEncoder.encode(key);
			@SuppressWarnings("deprecation")
			String encodedValue = URLEncoder.encode(value);

			result.append(result.length() == 0 ? "?" : "&");
			result.append(encodedKey);
			result.append("=");
			result.append(encodedValue);
		}
		return result.toString();
	}

	/**
	 * Checks is string is null or empty
	 */
	public static boolean isNullOrEmpty(String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * Safely checks if two strings are equal (any argument can be null)
	 */
	public static boolean equal(String str1, String str2) {
		return str1 != null && str2 != null && str1.equals(str2);
	}

	/**
	 * Creates a simple json string from key and value
	 */
	public static String asJson(String key, Object value) {
		try {
			JSONObject json = new JSONObject();
			json.put(key, value);
			return json.toString();
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while creating json-string { %s:%s }", key, value);
			return null;
		}
	}

	/**
	 * Converts a hex String to a byte array.
	 */
	public static byte[] hexToBytes(String hex) {
		int length = hex.length();
		byte[] ret = new byte[length / 2];
		for (int i = 0; i < length; i += 2) {
			ret[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
		}
		return ret;
	}
}
