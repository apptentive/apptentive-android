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
	private boolean isAppForeground = false, paused = true;
	private Handler handler = new Handler();

	public ApptentiveActivityLifecycleCallbacks(Application application) {
		appContext = application;
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		Log.e("onActivityCreated(%s)", activity.toString());
		if (runningActivities.getAndIncrement() == 0) {
			appLaunched(activity);
		}

		Log.e("==> Running Activities:    %d", runningActivities);
	}

	@Override
	public void onActivityStarted(Activity activity) {
		Log.e("onActivityStarted(%s)", activity.toString());
	}

	@Override
	public void onActivityResumed(Activity activity) {
		Log.e("onActivityResumed(%s)", activity.toString());
		paused = false;
		boolean wasAppBackground = !isAppForeground;
		isAppForeground = true;

		if (checkFgBgRoutine != null)
			handler.removeCallbacks(checkFgBgRoutine);

		if (wasAppBackground && foregroundActivities.getAndIncrement() == 0) {
			appBecomeForeground();
		} else {
			Log.d("application is still in foreground");
		}


		Log.e("==> Foreground Activities: %d", foregroundActivities);
		MessageManager.setCurrentForgroundActivity(activity);
	}

	@Override
	public void onActivityPaused(Activity activity) {
		Log.e("onActivityPaused(%s)", activity.toString());

		paused = true;

		foregroundActivities.decrementAndGet();
		if (foregroundActivities.decrementAndGet() < 0) {
			Log.a("Incorrect number of foreground Activities encountered. Resetting to 0.");
			foregroundActivities.set(0);
		}
		Log.e("==> Foreground Activities: %d", foregroundActivities);

		if (checkFgBgRoutine != null) {
			handler.removeCallbacks(checkFgBgRoutine);
		}
      /* When one activity transits to another one, there is a brief period durong which the former
      * is paused but the latter has not yet resumed. To prevent flase negative, check rountine is
      * conducted delayed
      */
		handler.postDelayed(checkFgBgRoutine = new Runnable() {
			@Override
			public void run() {
				if (isAppForeground && paused && foregroundActivities.get() == 0) {
					isAppForeground = false;
					appBecomeBackground();
				} else {
					Log.d("application is still in foreground");
				}
			}
		}, 500);

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
	public void onActivityDestroyed(Activity activity) {
		Log.e("onActivityDestroyed(%s)", activity.toString());

		if (runningActivities.decrementAndGet() < 0) {
			Log.a("Incorrect number of running Activities encountered. Resetting to 0.");
			runningActivities.set(0);
		}
		if (runningActivities.get() == 0) {
			appExited(activity);
		}
		Log.e("==> Running Activities:    %d", runningActivities);
	}

	private void appBecomeForeground() {
		Log.e("App went to foreground.");
		ApptentiveInternal.appIsInForeground = true;
		PayloadSendWorker.appWentToForeground(appContext);
		MessagePollingWorker.appWentToForeground(appContext);
	}

	private void appBecomeBackground() {
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
