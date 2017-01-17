package com.apptentive.android.sdk.util;

import static com.apptentive.android.sdk.debug.Assert.*;

/**
 * A collection of useful object-related functions
 */
public final class ObjectUtils {

	@SuppressWarnings("unchecked")
	public static <T> T as(Object object, Class<T> cls) {
		return cls.isInstance(object) ? (T) object : null;
	}

	public static <T> T notNull(T object, String message) {
		assertNotNull(object, message);
		return object;
	}
}
