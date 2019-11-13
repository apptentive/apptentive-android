package com.apptentive.android.sdk.debug;

import androidx.annotation.NonNull;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ErrorMetrics {
	private static final Object[] EMPTY_DATA = new Object[0];

	private static final List<OnErrorListener> listeners = new ArrayList<>();

	public static void logException(Throwable e) {
		logException(e, EMPTY_DATA);
	}

	public static void logException(Throwable e, Object... customData) {
		logException(e, ObjectUtils.toMap(customData));
	}

	public static void logException(Throwable e, Map<String, Object> customData) {
		for (OnErrorListener listener : listeners) {
			notifyListener(listener, e, customData);
		}
	}

	private static void notifyListener(OnErrorListener listener, Throwable e, Map<String, Object> customData) {
		try {
			listener.onError(e, Thread.currentThread(), customData);
		} catch (Exception ex) {
			ApptentiveLog.e(ex, "Exception while notifying listener " + listener);
		}
	}

	public static void registerListener(@NonNull OnErrorListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("Listener is null");
		}
		listeners.add(listener);
	}

	public static void unregisterListener(@NonNull OnErrorListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("Listener is null");
		}
		listeners.remove(listener);
	}

	public interface OnErrorListener {
		void onError(Throwable e, Thread t, Map<String, Object> customData);
	}
}
