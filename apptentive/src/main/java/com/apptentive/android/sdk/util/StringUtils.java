package com.apptentive.android.sdk.util;

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
				android.util.Log.e("Lunar", "Error while formatting String: " + e.getMessage()); // FIXME: better system loggingb
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
	 * Returns <code>true</code> is string is null or empty
	 */
	public static boolean isNullOrEmpty(String str) {
		return str == null || str.length() == 0;
	}
}
