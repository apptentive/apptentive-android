/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.apptentive.android.sdk.debug.AsyncLogWriter;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.threading.DispatchQueue;

import java.io.File;

public class ApptentiveLog {
	private static final String TAG = "Apptentive";
	private static Level logLevel = Level.INFO;
	private static boolean shouldSanitizeLogMessages = true;
	private static LogListener logListener;

	public static Level getLogLevel() {
		return logLevel;
	}

	static void initializeLogWriter(Context context, int logHistorySize) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		logListener = new AsyncLogWriter(getLogsDirectory(context), logHistorySize);
	}

	static boolean isLogWriterInitialized() {
		return logListener != null;
	}

	public static void overrideLogLevel(Level level) {
		ApptentiveLog.logLevel = level;
	}

	public static boolean shouldSanitizeLogMessages() {
		return shouldSanitizeLogMessages;
	}

	public static void setShouldSanitizeLogMessages(boolean shouldSanitizeLogMessages) {
		ApptentiveLog.shouldSanitizeLogMessages = shouldSanitizeLogMessages;
	}

	public static Object hideIfSanitized(Object value) {
		return value != null && shouldSanitizeLogMessages ? "<HIDDEN>" : value;
	}

	private static void log(Level level, @Nullable ApptentiveLogTag tag, Throwable throwable, String message, Object... args) {
		try {
			logGuarded(level, tag, throwable, message, args);
		} catch (Exception e) {
			// we don't care if this one fails in unit test: in fact it's better if unit test fails here
			android.util.Log.println(Log.ERROR, TAG, "Exception while trying to log a message: " + e.getMessage());
		}
	}

	private static void logGuarded(Level level, ApptentiveLogTag tag, Throwable throwable, String message, Object... args) {
		if (args != null && args.length > 0 && message != null && message.length() > 0) {
			try {
				message = String.format(message, args);
			} catch (Exception e) {
				message = "Error formatting log message: " + message;
				level = Level.ERROR;
			}
		}

		StringBuilder extra = null;

		// add thread name if logging of the UI-thread
		if (!DispatchQueue.isMainQueue()) {
			extra = new StringBuilder()
					.append('[')
					.append(Thread.currentThread().getName())
					.append(']');
		}

		// custom tag
		if (tag != null) {
			if (extra == null) {
				extra = new StringBuilder();
			} else {
				extra.append(' ');
			}
			extra
					.append('[')
					.append(tag.toString())
					.append(']');
		}

		if (extra != null) {
			message = extra.append(' ').append(message).toString();
		}

		log0(level, message);
		if (throwable != null) {
			log0(level, StringUtils.getStackTrace(throwable));
		}
	}

	private static void log0(Level level, String message) {
		try {
			if (canLog(level)) {
				android.util.Log.println(level.getAndroidLevel(), TAG, message);
			}
		} catch (Exception e) {
			System.out.println(message); // fallback for unit-test
		}

		if (logListener != null) {
			logListener.onLogMessage(level, message);
		}
	}

	public static @NonNull File getLogsDirectory(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}
		return new File(context.getCacheDir(), "com.apptentive.logs");
	}

	public static boolean canLog(Level level) {
		return logLevel.canLog(level);
	}

	public static void v(ApptentiveLogTag tag, String message, Object... args) {
		log(Level.VERBOSE, tag, null, message, args);
	}

	public static void v(ApptentiveLogTag tag, Throwable throwable, String message, Object... args) {
		log(Level.VERBOSE, tag, throwable, message, args);
	}

	public static void v(String message, Object... args) {
		log(Level.VERBOSE, null, null, message, args);
	}

	public static void v(Throwable throwable, String message, Object... args) {
		log(Level.VERBOSE, null, throwable, message, args);
	}

	public static void d(ApptentiveLogTag tag, String message, Object... args) {
		log(Level.DEBUG, tag, null, message, args);
	}

	public static void d(ApptentiveLogTag tag, Throwable throwable, String message, Object... args) {
		log(Level.DEBUG, tag, throwable, message, args);
	}

	public static void d(String message, Object... args) {
		log(Level.DEBUG, null, null, message, args);
	}

	public static void d(Throwable throwable, String message, Object... args) {
		log(Level.DEBUG, null, throwable, message, args);
	}

	public static void i(ApptentiveLogTag tag, String message, Object... args) {
		log(Level.INFO, tag, null, message, args);
	}

	public static void i(ApptentiveLogTag tag, Throwable throwable, String message, Object... args) {
		log(Level.INFO, tag, throwable, message, args);
	}

	public static void i(String message, Object... args) {
		log(Level.INFO, null, null, message, args);
	}

	public static void i(Throwable throwable, String message, Object... args) {
		log(Level.INFO, null, throwable, message, args);
	}

	public static void w(ApptentiveLogTag tag, String message, Object... args) {
		log(Level.WARN, tag, null, message, args);
	}

	public static void w(ApptentiveLogTag tag, Throwable throwable, String message, Object... args) {
		log(Level.WARN, tag, throwable, message, args);
	}

	public static void w(String message, Object... args) {
		log(Level.WARN, null, null, message, args);
	}

	public static void w(Throwable throwable, String message, Object... args) {
		log(Level.WARN, null, throwable, message, args);
	}

	public static void e(ApptentiveLogTag tag, String message, Object... args) {
		log(Level.ERROR, tag, null, message, args);
	}

	public static void e(ApptentiveLogTag tag, Throwable throwable, String message, Object... args) {
		log(Level.ERROR, tag, throwable, message, args);
	}

	public static void e(String message, Object... args) {
		log(Level.ERROR, null, null, message, args);
	}

	public static void e(Throwable throwable, String message, Object... args) {
		log(Level.ERROR, null, throwable, message, args);
	}

	public static void a(ApptentiveLogTag tag, String message, Object... args) {
		log(Level.ASSERT, tag, null, message, args);
	}

	public static void a(ApptentiveLogTag tag, Throwable throwable, String message, Object... args) {
		log(Level.ASSERT, tag, throwable, message, args);
	}

	public static void a(String message, Object... args) {
		log(Level.ASSERT, null, null, message, args);
	}

	public static void a(Throwable throwable, String message, Object... args) {
		log(Level.ASSERT, null, throwable, message, args);
	}

	public enum Level {
		VERBOSE("V", Log.VERBOSE, Log.VERBOSE),
		DEBUG("D", Log.DEBUG, Log.DEBUG),
		INFO("I", Log.INFO, Log.INFO),
		WARN("W", Log.WARN, Log.WARN),
		ERROR("E", Log.ERROR, Log.ERROR),
		ASSERT("A", Log.ASSERT, Log.ASSERT),
		UNKNOWN("?", -1, -1);

		private final int level;
		private final int androidLevel;
		private final String shortName;

		Level(String shortName, int level, int androidLevel) {
			this.shortName = shortName;
			this.level = level;
			this.androidLevel = androidLevel;
		}

		public String getShortName() {
			return shortName;
		}

		public int getAndroidLevel() {
			return androidLevel;
		}

		public int getLevel() {
			return level;
		}

		public static Level parse(String level) {
			if (!StringUtils.isNullOrEmpty(level)) {
				try {
					return Level.valueOf(level);
				} catch (Exception ignored) {
				}
			}
			return UNKNOWN;
		}

		/**
		 * If this object is the current level, returns true if the Level passed in is of a sufficient level to be logged.
		 *
		 * @return true if "level" can be logged.
		 */
		public boolean canLog(Level level) {
			return level.level >= this.level;
		}
	}

	public interface LogListener {
		void onLogMessage(@NonNull Level level, @NonNull String message);
	}
}
