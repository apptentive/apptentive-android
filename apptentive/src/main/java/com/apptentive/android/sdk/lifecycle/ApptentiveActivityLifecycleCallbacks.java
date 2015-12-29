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

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.MessagePollingWorker;
import com.apptentive.android.sdk.storage.PayloadSendWorker;

/**
 * @author Sky Kelsey
 */
public class ApptentiveActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

	private Context appContext;
	private int runningActivities;
	private int foregroundActivities;

	public ApptentiveActivityLifecycleCallbacks(Application application) {
		this.appContext = application;
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		Log.e("onActivityCreated(%s)", activity.toString());
		if (runningActivities == 0) {
			appLaunched(activity);
		}
		runningActivities++;
		Log.e("==> Running Activities:    %d", runningActivities);
	}

	@Override
	public void onActivityStarted(Activity activity) {
		Log.e("onActivityStarted(%s)", activity.toString());
	}

	@Override
	public void onActivityResumed(Activity activity) {
		Log.e("onActivityResumed(%s)", activity.toString());
		if (foregroundActivities == 0) {
			appWentToForeground();
		}
		foregroundActivities++;
		Log.e("==> Foreground Activities: %d", foregroundActivities);
		MessageManager.setCurrentForgroundActivity(activity);
	}

	@Override
	public void onActivityPaused(Activity activity) {
		Log.e("onActivityPaused(%s)", activity.toString());
		MessageManager.setCurrentForgroundActivity(null);

		foregroundActivities--;
		if (foregroundActivities < 0) {
			Log.a("Incorrect number of foreground Activities encountered. Resetting to 0.");
			foregroundActivities = 0;
		}
		if (foregroundActivities == 0) {
			appWentToBackground();
		}
		Log.e("==> Foreground Activities: %d", foregroundActivities);
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
		runningActivities--;
		if (runningActivities < 0) {
			Log.a("Incorrect number of running Activities encountered. Resetting to 0.");
			runningActivities = 0;
		}
		if (runningActivities == 0) {
			appExited(activity);
		}
		Log.e("==> Running Activities:    %d", runningActivities);
	}

	private void appWentToForeground() {
		Log.e("App went to foreground.");
		ApptentiveInternal.appIsInForeground = true;
		PayloadSendWorker.appWentToForeground(appContext);
		MessagePollingWorker.appWentToForeground(appContext);
	}

	private void appWentToBackground() {
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
