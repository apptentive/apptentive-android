/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.debug.ErrorMetrics;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

import static com.apptentive.android.sdk.ApptentiveHelper.dispatchOnConversationQueue;
import static com.apptentive.android.sdk.ApptentiveNotifications.*;

/**
 * 1. Keeps track of whether the app is in the foreground. It does this by counting the number of active Activities.
 * 2 Tells the SDK when the app goes to the background (exits), or comes to the foreground (launches).
 * 3. Tells the SDK when an Activity starts or resumes, so the SDK can hold a weak reference to the top Activity.
 */
public class ApptentiveActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

	// Holds reference to the current foreground activity of the host app
	private WeakReference<Activity> currentTaskStackTopActivity;

	private AtomicInteger foregroundActivities = new AtomicInteger(0);

	/**
	 * A <code>Runnable</code> that is <code>postDelayed()</code> in <code>onActivityStopped()</code> if the <code>foregroundActivities</code> is 0. When it runs, if the count is still 0, it fires <code>appEnteredBackground()</code>
	 */
	private Runnable checkFgBgRoutine;
	/**
	 * Set to false when the app goes to the background.
	 */
	private boolean isAppForeground;

	private boolean callbacksRegistered;

	private Handler delayedChecker = new Handler();

	private static final long CHECK_DELAY_SHORT = 1000;

	private ApptentiveActivityLifecycleCallbacks() {
	}

	public static synchronized void register(Application application) {
		if (application == null) {
			throw new IllegalArgumentException("Application is null");
		}

		Holder.INSTANCE.registerCallbacks(application);
	}

	public static @Nullable Activity getCurrentTopActivity() {
		WeakReference<Activity> reference = Holder.INSTANCE.currentTaskStackTopActivity;
		return reference != null ? reference.get() : null;
	}

	private void registerCallbacks(Application application) {
		if (!callbacksRegistered) {
			application.registerActivityLifecycleCallbacks(this);
			callbacksRegistered = true;
		} else {
			ApptentiveLog.w("Apptentive Activity callbacks already registered.");
			if (isAppForeground) {
				dispatchOnConversationQueue(new DispatchTask() {
                    @Override
                    protected void execute() {
                        ApptentiveLog.d("Sending missing foreground notification.");
                        ApptentiveNotificationCenter.defaultCenter()
                                .postNotification(NOTIFICATION_APP_ENTERED_FOREGROUND);
                    }
                });
			}
		}
	}

	@Override
	public void onActivityStarted(final Activity activity) {
		boolean wasAppBackground = !isAppForeground;
		isAppForeground = true;

		if (checkFgBgRoutine != null) {
			delayedChecker.removeCallbacks(checkFgBgRoutine);
			checkFgBgRoutine = null;
		}

		if (foregroundActivities.getAndIncrement() == 0 && wasAppBackground) {
			appEnteredForeground();
		}

		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				// Set current foreground activity reference whenever a new activity is started
				currentTaskStackTopActivity = new WeakReference<>(activity);

				ApptentiveNotificationCenter.defaultCenter()
						.postNotification(NOTIFICATION_ACTIVITY_STARTED, NOTIFICATION_KEY_ACTIVITY, activity);
			}
		});
	}

	@Override
	public void onActivityResumed(final Activity activity) {
		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				// Set current foreground activity reference whenever a new activity is started
				currentTaskStackTopActivity = new WeakReference<>(activity);

				ApptentiveNotificationCenter.defaultCenter()
						.postNotification(NOTIFICATION_ACTIVITY_RESUMED, NOTIFICATION_KEY_ACTIVITY, activity);
			}
		});
	}

	@Override
	public void onActivityPaused(final Activity activity) {

	}

	@Override
	public void onActivityDestroyed(final Activity activity) {
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

	}

	/**
	 * Decrements the count of running Activities. If it is now 0, start a task that will check again
	 * after a small delay. If that task still finds 0 running Activities, it will trigger an <code>appEnteredBackground()</code>
	 * @param activity
	 */
	@Override
	public void onActivityStopped(final Activity activity) {
		if (foregroundActivities.decrementAndGet() < 0) {
			ApptentiveLog.e("Incorrect number of foreground Activities encountered. Resetting to 0.");
			foregroundActivities.set(0);
		}

		if (checkFgBgRoutine != null) {
			delayedChecker.removeCallbacks(checkFgBgRoutine);
		}

		/* When one activity transits to another one, there is a brief period during which the former
		 * is paused but the latter has not yet resumed. To prevent false negative, check routine is
		 * delayed
		 */
		delayedChecker.postDelayed(checkFgBgRoutine = new Runnable() {
			@Override
			public void run() {
				try {
					if (foregroundActivities.get() == 0 && isAppForeground) {
						appEnteredBackground();
						isAppForeground = false;
					}
				} catch (Exception e) {
					ApptentiveLog.e(e, "Exception in delayed checking");
					ErrorMetrics.logException(e);
				}
			}
		}, CHECK_DELAY_SHORT);

		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				ApptentiveNotificationCenter.defaultCenter()
						.postNotification(NOTIFICATION_ACTIVITY_STOPPED, NOTIFICATION_KEY_ACTIVITY, activity);
			}
		});
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

	}

	private void appEnteredForeground() {
		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				ApptentiveLog.d("App went to foreground.");
				ApptentiveNotificationCenter.defaultCenter()
						.postNotification(NOTIFICATION_APP_ENTERED_FOREGROUND);
			}
		});
	}

	private void appEnteredBackground() {
		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				ApptentiveLog.d("App went to background.");
				currentTaskStackTopActivity = null;

				ApptentiveNotificationCenter.defaultCenter()
						.postNotification(NOTIFICATION_APP_ENTERED_BACKGROUND);
			}
		});
	}

	public static ApptentiveActivityLifecycleCallbacks getInstance() {
		return Holder.INSTANCE;
	}

	private static class Holder {
		private static final ApptentiveActivityLifecycleCallbacks INSTANCE = new ApptentiveActivityLifecycleCallbacks();
	}
}
