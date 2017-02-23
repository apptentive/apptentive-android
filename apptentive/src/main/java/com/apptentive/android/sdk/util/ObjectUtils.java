package com.apptentive.android.sdk.util;

/**
 * A collection of useful object-related functions
 */
public final class ObjectUtils {
	/**
	 * Attempts to cast <code>object</code> to class <code>cls</code>.
	 * Returns <code>null</code> if cast is impossible.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T as(Object object, Class<T> cls) {
		return cls.isInstance(object) ? (T) object : null;
	}
}