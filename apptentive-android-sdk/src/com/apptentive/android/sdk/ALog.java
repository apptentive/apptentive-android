/*
 * ALog.java
 *
 * Created by Sky Kelsey on 2011-05-14.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk;

import android.util.Log;

import java.util.IllegalFormatException;

public class ALog {

	public static final int VERBOSE = 2;
	public static final int DEBUG   = 3;
	public static final int INFO    = 4;
	public static final int WARN    = 5;
	public static final int ERROR   = 6;
	public static final int ASSERT  = 7;

	private String tag;

	public ALog(Class clazz){
		this.tag = clazz.getSimpleName();
	}

	private void doLog(int level, Throwable throwable, String message, Object... args){
		if(throwable != null){
			if(throwable.getMessage() != null){
				Log.println(level, tag, throwable.getMessage());
			}
			Log.println(level, tag, Log.getStackTraceString(throwable));
		}
		if(message != null){
			if(args.length > 0){
				try{
					message = String.format(message, args);
				}catch(IllegalFormatException e){
					message = "Error formatting log message [level="+level+"]: "+message;
					level = ERROR;
				}
			}
			Log.println(level, tag, message);
		}
	}

	public void v(String message, Object... args){
		doLog(VERBOSE, null, message, args);
	}
	public void v(String message, Throwable throwable, Object... args){
		doLog(VERBOSE, throwable, message, args);
	}

	public void d(String message, Object... args){
		doLog(DEBUG, null, message, args);
	}
	public void d(String message, Throwable throwable, Object... args){
		doLog(DEBUG, throwable, message, args);
	}

	public void i(String message, Object... args){
		doLog(INFO, null, message, args);
	}
	public void i(String message, Throwable throwable, Object... args){
		doLog(INFO, throwable, message, args);
	}

	public void w(String message, Object... args){
		doLog(WARN, null, message, args);
	}
	public void w(String message, Throwable throwable, Object... args){
		doLog(WARN, throwable, message, args);
	}

	public void e(String message, Object... args){
		doLog(ERROR, null, message, args);
	}
	public void e(String message, Throwable throwable, Object... args){
		doLog(ERROR, throwable, message, args);
	}

	public void a(String message, Object... args){
		doLog(ASSERT, null, message, args);
	}
	public void a(String message, Throwable throwable, Object... args){
		doLog(ASSERT, throwable, message, args);
	}
}

