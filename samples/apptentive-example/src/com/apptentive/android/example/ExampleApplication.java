/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.example;

import android.app.Application;

import com.apptentive.android.sdk.Apptentive;

public class ExampleApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Apptentive.register(this);
	}
}
