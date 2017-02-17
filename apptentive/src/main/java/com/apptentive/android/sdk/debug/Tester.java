package com.apptentive.android.sdk.debug;

import com.apptentive.android.sdk.ApptentiveLog;

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
			notifyEvent(name);
		}
	}

	public static void dispatchDebugEvent(String name, Object arg) {
		if (isListeningForDebugEvents()) {
			notifyEvent(name, arg);
		}
	}

	public static void dispatchDebugEvent(String name, boolean arg) {
		if (isListeningForDebugEvents()) {
			notifyEvent(name, arg);
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
			notifyEvent(EVT_EXCEPTION, e.getClass().getName(), e.getMessage(), stackTrace.toString());
		}
	}

	private static void notifyEvent(String name, Object... args) {
		try {
			instance.listener.onDebugEvent(name, args);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Error while dispatching debug event: %s", name);
		}
	}
}
