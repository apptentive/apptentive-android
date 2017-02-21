/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.os.Looper;
import android.util.Log;

import java.util.IllegalFormatException;

public class ApptentiveLog {
	private static final String TAG = "Apptentive";

	private static Level logLevel = Level.DEFAULT;

	public static void overrideLogLevel(Level level) {
		ApptentiveLog.logLevel = level;
	}

	private static void doLog(Level level, Throwable throwable, String message, Object... args){
		if(canLog(level) && message != null){
			if(args.length > 0){
				try{
					message = String.format(message, args);
				}catch(IllegalFormatException e){
					message = "Error formatting log message [level="+level+"]: "+message;
					level = Level.ERROR;
				}
			}
			// add thread name if logging of the UI-thread
			if (Looper.getMainLooper() != null && Looper.getMainLooper().getThread() != Thread.currentThread()) {
				message = String.format("[%s] %s", Thread.currentThread().getName(), message);
			}
			android.util.Log.println(level.getLevel(), TAG, message);
			if(throwable != null){
				if(throwable.getMessage() != null){
					android.util.Log.println(level.getLevel(), TAG, throwable.getMessage());
				}
				while(throwable != null) {
					android.util.Log.println(level.getLevel(), TAG, android.util.Log.getStackTraceString(throwable));
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
			doLog(Level.VERBOSE, null, message, args);
		}
	}

	public static void v(String message, Object... args){
		doLog(Level.VERBOSE, null, message, args);
	}
	public static void v(String message, Throwable throwable, Object... args){
		doLog(Level.VERBOSE, throwable, message, args);
	}

	public static void d(ApptentiveLogTag tag, String message, Object... args){
		if (tag.enabled) {
			doLog(Level.DEBUG, null, message, args);
		}
	}

	public static void d(String message, Object... args){
		doLog(Level.DEBUG, null, message, args);
	}
	public static void d(String message, Throwable throwable, Object... args){
		doLog(Level.DEBUG, throwable, message, args);
	}

	public static void i(ApptentiveLogTag tag, String message, Object... args){
		if (tag.enabled) {
			doLog(Level.INFO, null, message, args);
		}
	}
	public static void i(String message, Object... args){
		doLog(Level.INFO, null, message, args);
	}
	public static void i(String message, Throwable throwable, Object... args){
		doLog(Level.INFO, throwable, message, args);
	}

	public static void w(ApptentiveLogTag tag, String message, Object... args){
		if (tag.enabled) {
			doLog(Level.WARN, null, message, args);
		}
	}
	public static void w(String message, Object... args){
		doLog(Level.WARN, null, message, args);
	}
	public static void w(String message, Throwable throwable, Object... args){
		doLog(Level.WARN, throwable, message, args);
	}

	public static void e(ApptentiveLogTag tag, String message, Object... args){
		if (tag.enabled) {
			doLog(Level.ERROR, null, message, args);
		}
	}
	public static void e(String message, Object... args){
		doLog(Level.ERROR, null, message, args);
	}
	public static void e(String message, Throwable throwable, Object... args){
		doLog(Level.ERROR, throwable, message, args);
	}
	public static void e(Throwable throwable, String message, Object... args){
		doLog(Level.ERROR, throwable, message, args);
	}

	public static void a(String message, Object... args){
		doLog(Level.ASSERT, null, message, args);
	}
	public static void a(String message, Throwable throwable, Object... args){
		doLog(Level.ASSERT, throwable, message, args);
	}

	public enum Level {
		VERBOSE(Log.VERBOSE),
		DEBUG(Log.DEBUG),
		INFO(Log.INFO),
		WARN(Log.WARN),
		ERROR(Log.ERROR),
		ASSERT(Log.ASSERT),
		DEFAULT(Log.INFO);

		private int level;

		private Level(int level) {
			this.level = level;
		}

		public int getLevel() {
			return level;
		}

		public static Level parse(String level) {
			try {
				return Level.valueOf(level);
			} catch (IllegalArgumentException e) {
				android.util.Log.println(Log.WARN, TAG, "Error parsing unknown ApptentiveLog.Level: " + level);
			}
			return DEFAULT;
		}

		/**
		 * If this object is the current level, returns true if the Level passed in is of a sufficient level to be logged.
		 * @return true if "level" can be logged.
		 */
		public boolean canLog(Level level) {
			return level.getLevel() >= getLevel();
		}
	}
}
