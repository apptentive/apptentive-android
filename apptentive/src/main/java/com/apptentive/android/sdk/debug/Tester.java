package com.apptentive.android.sdk.debug;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.ObjectUtils;

import java.util.Map;

import static com.apptentive.android.sdk.debug.TesterEvent.*;

public class Tester {
	private static Tester instance;

	private TesterEventListener listener;

	private Tester(TesterEventListener listener) {
		this.listener = listener;
	}

	////////////////////////////////////////////////////////////////
	// Instance

	public static void init(TesterEventListener delegate) {
		instance = new Tester(delegate);
	}

	public static void destroy() {
		instance = null;
	}

	////////////////////////////////////////////////////////////////
	// Events

	public static boolean isListeningForDebugEvents() {
		return instance != null && instance.listener != null;
	}

	public static void dispatchDebugEvent(String name) {
		if (isListeningForDebugEvents()) {
			notifyEvent(name, null);
		}
	}

	public static void dispatchDebugEvent(String name, boolean successful) {
		if (isListeningForDebugEvents()) {
			notifyEvent(name, ObjectUtils.toMap(EVT_KEY_SUCCESSFUL, successful));
		}
	}

	public static void dispatchDebugEvent(String name, String key, Object value) {
		if (isListeningForDebugEvents()) {
			notifyEvent(name, ObjectUtils.toMap(key, value));
		}
	}

	public static void dispatchDebugEvent(String name, String key1, Object value1, String key2, Object value2) {
		if (isListeningForDebugEvents()) {
			notifyEvent(name, ObjectUtils.toMap(key1, value1, key2, value2));
		}
	}

	public static void dispatchDebugEvent(String name, String key1, Object value1, String key2, Object value2, String key3, Object value3) {
		if (isListeningForDebugEvents()) {
			notifyEvent(name, ObjectUtils.toMap(key1, value1, key2, value2, key3, value3));
		}
	}

	public static void dispatchException(Throwable e) {
		if (isListeningForDebugEvents() && e != null) {
			StringBuilder stackTrace = new StringBuilder();
			StackTraceElement[] elements = e.getStackTrace();
			for (int i = 0; i < elements.length; ++i) {
				stackTrace.append(elements[i]);
				if (i < elements.length - 1) {
					stackTrace.append('\n');
				}
			}
			notifyEvent(EVT_EXCEPTION, ObjectUtils.toMap(
				"class,", e.getClass().getName(),
				"message", e.getMessage(),
				"stacktrace", stackTrace.toString()));
		}
	}

	private static void notifyEvent(String name, Map<String, Object> userInfo) {
		try {
			instance.listener.onDebugEvent(name, userInfo);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Error while dispatching debug event: %s", name);
		}
	}
}
