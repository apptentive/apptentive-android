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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * A collection of useful string-related functions
 */
public final class StringUtils {
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss:SSS", Locale.US);

	/**
	 * Safe <code>String.format</code>
	 */
	public static String format(String format, Object... args) {
		if (format != null && args != null && args.length > 0) {
			try {
				return String.format(format, args);
			} catch (Exception e) {
				android.util.Log.e("Apptentive", "Error while formatting String: " + e.getMessage()); // TODO: better system logging
			}
		}

		return format;
	}

	public static String getStackTrace(Throwable t) {
		Writer writer = new StringWriter();
		t.printStackTrace(new PrintWriter(writer));
		return writer.toString();
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

	public static String toPrettyDate(double timeInSeconds) {
		long timeInMillis = (long) (1000L * timeInSeconds);
		return DATE_FORMAT.format(new Date(timeInMillis));
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
	 * Checks is string is null or empty
	 */
	public static boolean isNullOrEmpty(String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * Checks is string is null or blank(only white spaces)
	 */
	public static boolean isNullOrBlank(String str) {
		return str == null || isNullOrEmpty(str.trim());
	}

	/**
	 * Safely trims input string
	 */
	public static String trim(String str) {
		return str != null && str.length() > 0 ? str.trim() : str;
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

	//region Pretty print

	public static String table(Object[][] rows) {
		return table(rows, null);
	}

	public static String table(Object[][] rows, String title) {
		int[] columnSizes = new int[rows[0].length];
		for (Object[] row : rows) {
			for (int i = 0; i < row.length; ++i) {
				columnSizes[i] = Math.max(columnSizes[i], toString(row[i]).length());
			}
		}

		StringBuilder line = new StringBuilder();
		int totalSize = 0;
		for (int i = 0; i < columnSizes.length; ++i) {
			totalSize += columnSizes[i];
		}
		totalSize += columnSizes.length > 0 ? (columnSizes.length - 1) * " | ".length() : 0;
		while (totalSize-- > 0) {
			line.append('-');
		}

		StringBuilder result = new StringBuilder(line);

		for (Object[] row : rows) {
			result.append("\n");

			for (int i = 0; i < row.length; ++i) {
				if (i > 0) {
					result.append(" | ");
				}

				result.append(String.format("%-" + columnSizes[i] + "s", row[i]));
			}
		}

		result.append("\n").append(line);
		return result.toString();
	}

	//endregion

	//region Parsing

	public static int parseInt(String value, int defaultValue) {
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	//endregion

	//region Device identifiers

	public static String randomAndroidID() {
		Random random = new Random();
		long lo = ((long) random.nextInt()) & 0xffffffffL;
		long hi = ((long) random.nextInt()) << 32L;
		long number = hi | lo;
		return Long.toHexString(number);
	}

	//endregion
}
