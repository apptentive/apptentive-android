package com.apptentive.android.sdk.util;

import java.util.HashMap;
import java.util.Map;

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

	public static Map<String, Object> toMap(Object... args) {
		if (args.length % 2 != 0) {
			throw new IllegalArgumentException("Invalid args");
		}

		Map<String, Object> map = new HashMap<>();
		for (int i = 0; i < args.length; i += 2) {
			map.put((String) args[i], args[i + 1]);
		}

		return map;
	}
}