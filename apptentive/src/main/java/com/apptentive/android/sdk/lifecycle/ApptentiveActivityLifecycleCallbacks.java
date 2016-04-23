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

import java.util.concurrent.atomic.AtomicInteger;

public class ApptentiveActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

	private AtomicInteger runningActivities = new AtomicInteger(0);
	private AtomicInteger foregroundActivities = new AtomicInteger(0);

	private Runnable checkFgBgRoutine;
	private boolean isAppForeground;
	private Handler delayedChecker = new Handler();

	private static final long CHECK_DELAY_SHORT = 1000;

	public ApptentiveActivityLifecycleCallbacks() {
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		ApptentiveLog.d("onActivityCreated(%s)", activity.toString());
		ApptentiveLog.d("==> Running Activities:    %d", runningActivities.get());
	}

	@Override
	public void onActivityStarted(Activity activity) {
		ApptentiveLog.d("onActivityStarted(%s)", activity.toString());
		ApptentiveInternal.getInstance().onActivityStarted(activity);
	}

	@Override
	public void onActivityResumed(Activity activity) {
		ApptentiveLog.d("onActivityResumed(%s)", activity.toString());

		boolean wasAppBackground = !isAppForeground;
		isAppForeground = true;

		if (checkFgBgRoutine != null) {
			delayedChecker.removeCallbacks(checkFgBgRoutine);
			checkFgBgRoutine = null;
		}

		if (foregroundActivities.getAndIncrement() == 0 && wasAppBackground) {
			appEnteredForeground();
		} else {
			ApptentiveLog.d("application is still in foreground");
		}

		ApptentiveLog.d("==> Foreground Activities: %d", foregroundActivities.get());

		ApptentiveInternal.getInstance().onActivityResumed(activity);
	}

	@Override
	public void onActivityPaused(final Activity activity) {
		ApptentiveLog.d("onActivityPaused(%s)", activity.toString());

		if (foregroundActivities.decrementAndGet() < 0) {
			ApptentiveLog.a("Incorrect number of foreground Activities encountered. Resetting to 0.");
			foregroundActivities.set(0);
		}
		ApptentiveLog.d("==> Foreground Activities: %d", foregroundActivities.get());

		if (checkFgBgRoutine != null) {
			delayedChecker.removeCallbacks(checkFgBgRoutine);
		}
      /* When one activity transits to another one, there is a brief period durong which the former
      * is paused but the latter has not yet resumed. To prevent flase negative, check rountine is
      * conducted delayed
      */
		delayedChecker.postDelayed(checkFgBgRoutine = new Runnable() {
			@Override
			public void run() {
				if (foregroundActivities.get() == 0 && isAppForeground) {
					appEnteredBackground();
					isAppForeground = false;
				} else {
					ApptentiveLog.d("application is still in foreground");
				}
			}
		}, CHECK_DELAY_SHORT);


		ApptentiveInternal.getInstance().getMessageManager().setCurrentForgroundActivity(null);

	}

	@Override
	public void onActivityStopped(Activity activity) {
		ApptentiveLog.d("onActivityStopped(%s)", activity.toString());
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
		ApptentiveLog.d("onActivitySaveInstanceState(%s)", activity.toString());
	}

	@Override
	public void onActivityDestroyed(final Activity activity) {
		ApptentiveLog.d("onActivityDestroyed(%s)", activity.toString());

		ApptentiveInternal.getInstance().onActivityDestroyed(activity);

		if (runningActivities.decrementAndGet() < 0) {
			ApptentiveLog.a("Incorrect number of running Activities encountered. Resetting to 0.");
			runningActivities.set(0);
		}

		ApptentiveLog.d("==> Running Activities:    %d", runningActivities.get());
	}

	private void appEnteredForeground() {
		ApptentiveLog.d("App went to foreground.");
		ApptentiveInternal.getInstance().onAppEnterForeground();
		// Mark entering foreground as app launch
		appLaunched(ApptentiveInternal.getInstance().getApplicationContext());
	}

	private void appEnteredBackground() {
		ApptentiveLog.d("App went to background.");
		ApptentiveInternal.getInstance().onAppEnterBackground();
		// Mark entering background as app exit
		appExited(ApptentiveInternal.getInstance().getApplicationContext());
	}

	private void appLaunched(Context appContext) {
		ApptentiveLog.i("### App LAUNCH");
		ApptentiveInternal.getInstance().onAppLaunch(appContext);
	}

	private void appExited(Context appContext) {
		ApptentiveLog.i("### App EXIT");
		ApptentiveInternal.getInstance().onAppExit(appContext);
	}
}
