/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.example;

import android.app.Application;

import com.apptentive.android.sdk.Apptentive;

public class ExampleApplication extends Application {
	public static final String TAG = "ApptentiveExample";

	@Override
	public void onCreate() {
		super.onCreate();
		Apptentive.register(this, "", "");
	}
}
