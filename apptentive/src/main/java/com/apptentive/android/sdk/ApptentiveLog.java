/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.util.Log;

import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.threading.DispatchQueue;

import java.util.IllegalFormatException;

public class ApptentiveLog {
	private static final String TAG = "Apptentive";
	private static final LoggerImplementation LOGGER_IMPLEMENTATION = new LoggerImplementation() {
		@Override
		public void println(int priority, String tag, String msg) {
			//noinspection WrongConstant
			android.util.Log.println(priority, tag, msg);
		}

		@Override
		public String getStackTraceString(Throwable throwable) {
			return android.util.Log.getStackTraceString(throwable);
		}
	};

	private static Level logLevel = Level.INFO;

	public static Level getLogLevel() {
		return logLevel;
	}

	public static void overrideLogLevel(Level level) {
		ApptentiveLog.logLevel = level;
	}

	private static void doLog(Level level, ApptentiveLogTag tag, Throwable throwable, String message, Object... args){
		if(canLog(level) && message != null){
			if(args.length > 0){
				try{
					message = String.format(message, args);
				}catch(IllegalFormatException e){
					message = "Error formatting log message [level="+level+"]: "+message;
					level = Level.ERROR;
				}
			}

			String extra = null;

			// add thread name if logging of the UI-thread
			if (!DispatchQueue.isMainQueue()) {
				extra = '[' + Thread.currentThread().getName() + ']';
			}

			// custom tag
			if (tag != null) {
				if (extra == null) {
					extra = '[' + tag.toString() + ']';
				} else {
					extra += " [" + tag.toString() + ']';
				}
			}

			if (extra != null) {
				message = extra + " " + message;
			}


			LOGGER_IMPLEMENTATION.println(level.getAndroidLevel(), TAG, message);
			if(throwable != null){
				if(throwable.getMessage() != null){
					LOGGER_IMPLEMENTATION.println(level.getAndroidLevel(), TAG, throwable.getMessage());
				}
				while(throwable != null) {
					LOGGER_IMPLEMENTATION.println(level.getAndroidLevel(), TAG, LOGGER_IMPLEMENTATION.getStackTraceString(throwable));
					throwable = throwable.getCause();
				}
			}
		}
	}

	public static boolean canLog(Level level) {
		return logLevel.canLog(level);
	}

	public static void v(ApptentiveLogTag tag, String message, Object... args) {
		if (tag.enabled) {
			doLog(Level.VERBOSE, tag, null, message, args);
		}
	}
	public static void v(ApptentiveLogTag tag, Throwable throwable, String message, Object... args){
		if (tag.enabled) {
			doLog(Level.VERBOSE, tag, throwable, message, args);
		}
	}
	public static void v(String message, Object... args){
		doLog(Level.VERBOSE, null, null, message, args);
	}
	public static void v(Throwable throwable, String message, Object... args){
		doLog(Level.VERBOSE, null, throwable, message, args);
	}

	public static void d(ApptentiveLogTag tag, String message, Object... args){
		if (tag.enabled) {
			doLog(Level.DEBUG, tag, null, message, args);
		}
	}
	public static void d(ApptentiveLogTag tag, Throwable throwable, String message, Object... args){
		if (tag.enabled) {
			doLog(Level.DEBUG, tag, throwable, message, args);
		}
	}
	public static void d(String message, Object... args){
		doLog(Level.DEBUG, null, null, message, args);
	}
	public static void d(Throwable throwable, String message, Object... args){
		doLog(Level.DEBUG, null, throwable, message, args);
	}

	public static void i(ApptentiveLogTag tag, String message, Object... args){
		if (tag.enabled) {
			doLog(Level.INFO, tag, null, message, args);
		}
	}
	public static void i(ApptentiveLogTag tag, Throwable throwable, String message, Object... args){
		if (tag.enabled) {
			doLog(Level.INFO, tag, throwable, message, args);
		}
	}
	public static void i(String message, Object... args){
		doLog(Level.INFO, null, null, message, args);
	}
	public static void i(Throwable throwable, String message, Object... args){
		doLog(Level.INFO, null, throwable, message, args);
	}

	public static void w(ApptentiveLogTag tag, String message, Object... args){
		if (tag.enabled) {
			doLog(Level.WARN, tag, null, message, args);
		}
	}
	public static void w(ApptentiveLogTag tag, Throwable throwable, String message, Object... args){
		if (tag.enabled) {
			doLog(Level.WARN, tag, throwable, message, args);
		}
	}
	public static void w(String message, Object... args){
		doLog(Level.WARN, null, null, message, args);
	}
	public static void w(Throwable throwable, String message, Object... args){
		doLog(Level.WARN, null, throwable, message, args);
	}

	public static void e(ApptentiveLogTag tag, String message, Object... args){
		if (tag.enabled) {
			doLog(Level.ERROR, tag, null, message, args);
		}
	}
	public static void e(ApptentiveLogTag tag, Throwable throwable, String message, Object... args){
		if (tag.enabled) {
			doLog(Level.ERROR, tag, throwable, message, args);
		}
	}
	public static void e(String message, Object... args){
		doLog(Level.ERROR, null, null, message, args);
	}
	public static void e(Throwable throwable, String message, Object... args){
		doLog(Level.ERROR, null, throwable, message, args);
	}

	public static void a(ApptentiveLogTag tag, String message, Object... args){
		if (tag.enabled) {
			doLog(Level.ASSERT, tag, null, message, args);
		}
	}
	public static void a(ApptentiveLogTag tag, Throwable throwable, String message, Object... args){
		if (tag.enabled) {
			doLog(Level.ASSERT, tag, throwable, message, args);
		}
	}
	public static void a(String message, Object... args){
		doLog(Level.ASSERT, null, null, message, args);
	}
	public static void a(Throwable throwable, String message, Object... args){
		doLog(Level.ASSERT, null, throwable, message, args);
	}

	public enum Level {
		VERBOSE(Log.VERBOSE, Log.VERBOSE),
		DEBUG(Log.DEBUG, Log.DEBUG),
		INFO(Log.INFO, Log.INFO),
		WARN(Log.WARN, Log.WARN),
		ERROR(Log.ERROR, Log.ERROR),
		ASSERT(Log.ASSERT, Log.ASSERT),
		UNKNOWN(-1, -1);

		private int level;
		private int androidLevel;

		Level(int level, int androidLevel) {
			this.level = level;
			this.androidLevel = androidLevel;
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
				} catch (Exception e) {
					LOGGER_IMPLEMENTATION.println(Log.WARN, TAG, "Error parsing unknown ApptentiveLog.Level: " + level);
				}
			}
			return UNKNOWN;
		}

		/**
		 * If this object is the current level, returns true if the Level passed in is of a sufficient level to be logged.
		 * @return true if "level" can be logged.
		 */
		public boolean canLog(Level level) {
			return level.level >= this.level;
		}
	}

	interface LoggerImplementation {
		void println(int priority, String tag, String msg);
		String getStackTraceString(Throwable throwable);
	}
}
