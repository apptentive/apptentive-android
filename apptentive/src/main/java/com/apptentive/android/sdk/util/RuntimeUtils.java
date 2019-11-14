/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveLog;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

/**
 * Collection of helper functions for Android runtime queries.
 */
public class RuntimeUtils {
	private static ApplicationInfo cachedApplicationInfo;

	public synchronized static @NonNull ApplicationInfo getApplicationInfo(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		if (cachedApplicationInfo == null) {
			// cache the value once (since it won't change while the app is running)
			try {
				PackageManager packageManager = context.getPackageManager();
				PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
				android.content.pm.ApplicationInfo ai = packageInfo.applicationInfo;
				boolean debuggable = ai != null && (ai.flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
				int targetSdkVersion = ai != null ? ai.targetSdkVersion : 0;
				cachedApplicationInfo = new ApplicationInfo(packageInfo.versionName, packageInfo.versionCode, targetSdkVersion, debuggable);
				ApptentiveLog.v("Resolved application info: %s", cachedApplicationInfo);
			} catch (Exception e) {
				ApptentiveLog.e(e, "Exception while getting app info");
				logException(e);
				cachedApplicationInfo = ApplicationInfo.NULL;
			}
		}
		return cachedApplicationInfo;
	}

	public static String getAppVersionName(Context context) {
		return getApplicationInfo(context).getVersionName();
	}

	public static int getAppVersionCode(Context context) {
		return getApplicationInfo(context).getVersionCode();
	}

	/**
	 * Returns <code>true</code> is the app is running in a debug mode
	 */
	public static boolean isAppDebuggable(Context context) {
		return getApplicationInfo(context).isDebuggable();
	}

	/**
	 * @return true if class exists
	 */
	public static boolean classExists(@NonNull String name) {
		try {
			Class.forName(name);
			return true;
		} catch (Exception ignored) {
		}

		return false;
	}

	public static Class<?> classForName(String name) {
		try {
			return Class.forName(name);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Unable to get class with name '%s'", name);
			logException(e);
		}
		return null;
	}

	public static void overrideStaticFinalField(Class<?> cls, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
		Field instanceField = cls.getDeclaredField(fieldName);
		instanceField.setAccessible(true);

		Field modifiersField = getFieldModifiers();
		modifiersField.setAccessible(true);
		modifiersField.setInt(instanceField, instanceField.getModifiers() & ~Modifier.FINAL);

		instanceField.set(null, value);
	}

	private static Field getFieldModifiers() throws NoSuchFieldException {
		try {
			return Field.class.getDeclaredField("modifiers"); // logic tests
		} catch (Exception ignored) {
		}

		return Field.class.getDeclaredField("accessFlags"); // android instrumentation tests
	}

	public static List<Field> listFields(Class<?> cls, FieldFilter filter) {
		List<Field> result = new ArrayList<>();
		while (cls != null) {
			for (Field field : cls.getDeclaredFields()) {
				if (filter.accept(field)) {
					result.add(field);
				}
			}
			cls = cls.getSuperclass();
		}
		return result;
	}

	public interface FieldFilter {
		boolean accept(Field field);
	}
}
