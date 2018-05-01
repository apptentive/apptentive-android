/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import java.util.concurrent.atomic.AtomicInteger;

import static com.apptentive.android.sdk.ApptentiveHelper.checkConversationQueue;
import static com.apptentive.android.sdk.ApptentiveHelper.dispatchOnConversationQueue;

/**
 * 1. Keeps track of whether the app is in the foreground. It does this by counting the number of active Activities.
 * 2 Tells the SDK when the app goes to the background (exits), or comes to the foreground (launches).
 * 3. Tells the SDK when an Activity starts or resumes, so the SDK can hold a weak reference to the top Activity.
 */
public class ApptentiveActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

	private AtomicInteger foregroundActivities = new AtomicInteger(0);

	/**
	 * A <code>Runnable</code> that is <code>postDelayed()</code> in <code>onActivityStopped()</code> if the <code>foregroundActivities</code> is 0. When it runs, if the count is still 0, it fires <code>appEnteredBackground()</code>
	 */
	private Runnable checkFgBgRoutine;
	/**
	 * Set to false when the app goes to the background.
	 */
	private boolean isAppForeground;
	private Handler delayedChecker = new Handler();

	private static final long CHECK_DELAY_SHORT = 1000;

	public ApptentiveActivityLifecycleCallbacks() {
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
				ApptentiveInternal.getInstance().onActivityStarted(activity); // TODO: post a notification here
			}
		});
	}

	@Override
	public void onActivityResumed(final Activity activity) {
		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				ApptentiveInternal.getInstance().onActivityResumed(activity);  // TODO: post a notification here
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
				}
			}
		}, CHECK_DELAY_SHORT);

		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				ApptentiveInternal.getInstance().onActivityStopped(activity);
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
				ApptentiveInternal.getInstance().onAppEnterForeground();
				// Mark entering foreground as app launch
				appLaunched(ApptentiveInternal.getInstance().getApplicationContext());
			}
		});
	}

	private void appEnteredBackground() {
		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				ApptentiveLog.d("App went to background.");
				ApptentiveInternal.getInstance().onAppEnterBackground();  // TODO: post a notification here
				// Mark entering background as app exit
				appExited(ApptentiveInternal.getInstance().getApplicationContext());
			}
		});
	}

	private void appLaunched(Context appContext) {
		checkConversationQueue();
		ApptentiveInternal.getInstance().onAppLaunch(appContext);  // TODO: post a notification here
	}

	private void appExited(Context appContext) {
		checkConversationQueue();
		ApptentiveInternal.getInstance().onAppExit(appContext);  // TODO: post a notification here
	}
}
