/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
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

/**
 * @author Sky Kelsey
 */
public class ApptentiveActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

	private Context appContext;
	private AtomicInteger runningActivities = new AtomicInteger(0);
	private AtomicInteger foregroundActivities = new AtomicInteger(0);

	private Runnable checkFgBgRoutine;
	private Runnable checkAppExitRoutine;
	private boolean isAppForeground, running;
	private Handler delayedChecker = new Handler();

	private static final long CHECK_DELAY_SHORT = 500;
	private static final long CHECK_DELAY_LONG = 1000;

	public ApptentiveActivityLifecycleCallbacks(Context application) {
		appContext = application;
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		ApptentiveLog.e("onActivityCreated(%s)", activity.toString());

		if (checkAppExitRoutine != null) {
			delayedChecker.removeCallbacks(checkAppExitRoutine);
			checkAppExitRoutine = null;
		}

		if (runningActivities.getAndIncrement() == 0) {
			appLaunched(appContext);
		}

		ApptentiveLog.e("==> Running Activities:    %d", runningActivities.get());
	}

	@Override
	public void onActivityStarted(Activity activity) {
		ApptentiveLog.e("onActivityStarted(%s)", activity.toString());
		ApptentiveInternal.getInstance().onActivityStarted(activity);
	}

	@Override
	public void onActivityResumed(Activity activity) {
		ApptentiveLog.e("onActivityResumed(%s)", activity.toString());
		running = true;
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

		ApptentiveLog.e("==> Foreground Activities: %d", foregroundActivities.get());

		ApptentiveInternal.getInstance().onActivityResumed(activity);
	}

	@Override
	public void onActivityPaused(final Activity activity) {
		ApptentiveLog.e("onActivityPaused(%s)", activity.toString());

		running = false;

		if (foregroundActivities.decrementAndGet() < 0) {
			ApptentiveLog.a("Incorrect number of foreground Activities encountered. Resetting to 0.");
			foregroundActivities.set(0);
		}
		ApptentiveLog.e("==> Foreground Activities: %d", foregroundActivities.get());

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
				if (isAppForeground && !running) {
					isAppForeground = false;
					appEnteredBackground();
				} else {
					ApptentiveLog.d("application is still in foreground");
				}
			}
		}, CHECK_DELAY_SHORT);


		ApptentiveInternal.getInstance().getMessageManager().setCurrentForgroundActivity(null);

	}

	@Override
	public void onActivityStopped(Activity activity) {
		ApptentiveLog.e("onActivityStopped(%s)", activity.toString());
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
		ApptentiveLog.e("onActivitySaveInstanceState(%s)", activity.toString());
	}

	@Override
	public void onActivityDestroyed(final Activity activity) {
		ApptentiveLog.e("onActivityDestroyed(%s)", activity.toString());

		ApptentiveInternal.getInstance().onActivityDestroyed(activity);

		if (runningActivities.decrementAndGet() < 0) {
			ApptentiveLog.a("Incorrect number of running Activities encountered. Resetting to 0.");
			runningActivities.set(0);
		}

		if (checkAppExitRoutine != null) {
			delayedChecker.removeCallbacks(checkAppExitRoutine);
			checkAppExitRoutine = null;
		}

		delayedChecker.postDelayed(checkAppExitRoutine = new Runnable() {
			@Override
			public void run() {
				if (runningActivities.get() == 0) {
					appExited(appContext);
				} else {
					ApptentiveLog.d("application is still in foreground");
				}
			}
		}, CHECK_DELAY_LONG);

		ApptentiveLog.e("==> Running Activities:    %d", runningActivities.get());
	}

	private void appEnteredForeground() {
		ApptentiveLog.e("App went to foreground.");
		ApptentiveInternal.getInstance().onAppEnterForeground();
	}

	private void appEnteredBackground() {
		ApptentiveLog.e("App went to background.");
		ApptentiveInternal.getInstance().onAppEnterBackground();
	}

	private void appLaunched(Context appContext) {
		ApptentiveLog.e("### App LAUNCH");
		ApptentiveInternal.getInstance().onAppLaunch(appContext);
	}

	private void appExited(Context appContext) {
		ApptentiveLog.e("### App EXIT");
		ApptentiveInternal.getInstance().onAppExit(appContext);
	}
}
