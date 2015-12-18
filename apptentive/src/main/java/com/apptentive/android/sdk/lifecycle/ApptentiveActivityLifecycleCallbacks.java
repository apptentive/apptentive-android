/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.apptentive.android.sdk.Log;

/**
 * @author Sky Kelsey
 */
public class ApptentiveActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		Log.e("onActivityCreated(%s)", activity.toString());
	}

	@Override
	public void onActivityStarted(Activity activity) {
		Log.e("onActivityStarted(%s)", activity.toString());
	}

	@Override
	public void onActivityResumed(Activity activity) {
		Log.e("onActivityResumed(%s)", activity.toString());
	}

	@Override
	public void onActivityPaused(Activity activity) {
		Log.e("onActivityPaused(%s)", activity.toString());
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
	}
}
