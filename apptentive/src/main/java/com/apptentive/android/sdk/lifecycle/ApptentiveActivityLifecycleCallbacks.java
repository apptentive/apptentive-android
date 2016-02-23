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
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.MessagePollingWorker;
import com.apptentive.android.sdk.storage.PayloadSendWorker;

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

	public ApptentiveActivityLifecycleCallbacks(Application application) {
		appContext = application;
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		Log.e("onActivityCreated(%s)", activity.toString());

		if (checkAppExitRoutine != null) {
			delayedChecker.removeCallbacks(checkAppExitRoutine);
			checkAppExitRoutine = null;
		}

		if (runningActivities.getAndIncrement() == 0) {
			appLaunched(activity);
		}

		Log.e("==> Running Activities:    %d", runningActivities.get());
	}

	@Override
	public void onActivityStarted(Activity activity) {
		Log.e("onActivityStarted(%s)", activity.toString());
	}

	@Override
	public void onActivityResumed(Activity activity) {
		Log.e("onActivityResumed(%s)", activity.toString());
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
			Log.d("application is still in foreground");
		}


		Log.e("==> Foreground Activities: %d", foregroundActivities.get());
		MessageManager.setCurrentForgroundActivity(activity);
	}

	@Override
	public void onActivityPaused(Activity activity) {
		Log.e("onActivityPaused(%s)", activity.toString());

		running = false;

		foregroundActivities.decrementAndGet();
		if (foregroundActivities.decrementAndGet() < 0) {
			Log.a("Incorrect number of foreground Activities encountered. Resetting to 0.");
			foregroundActivities.set(0);
		}
		Log.e("==> Foreground Activities: %d", foregroundActivities.get());

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
					Log.d("application is still in foreground");
				}
			}
		}, CHECK_DELAY_SHORT);

		MessageManager.setCurrentForgroundActivity(null);

	}

	@Override
	public void onActivityStopped(Activity activity) {
		Log.e("onActivityStopped(%s)", activity.toString());
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
		Log.e("onActivitySaveInstanceState(%s)", activity.toString());
	}

	@Override
	public void onActivityDestroyed(final Activity activity) {
		Log.e("onActivityDestroyed(%s)", activity.toString());

		if (runningActivities.decrementAndGet() < 0) {
			Log.a("Incorrect number of running Activities encountered. Resetting to 0.");
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
					appExited(activity);
				} else {
					Log.d("application is still in foreground");
				}
			}
		}, CHECK_DELAY_LONG);

		Log.e("==> Running Activities:    %d", runningActivities.get());
	}

	private void appEnteredForeground() {
		Log.e("App went to foreground.");
		ApptentiveInternal.appIsInForeground = true;
		PayloadSendWorker.appWentToForeground(appContext);
		MessagePollingWorker.appWentToForeground(appContext);
	}

	private void appEnteredBackground() {
		Log.e("App went to background.");
		ApptentiveInternal.appIsInForeground = false;
		PayloadSendWorker.appWentToBackground();
		MessagePollingWorker.appWentToBackground();
	}

	private void appLaunched(Activity activity) {
		Log.e("### App LAUNCH");
		ApptentiveInternal.onAppLaunch(activity);
	}

	private void appExited(Activity activity) {
		Log.e("### App EXIT");
		ApptentiveInternal.onAppExit(activity);
	}
}
