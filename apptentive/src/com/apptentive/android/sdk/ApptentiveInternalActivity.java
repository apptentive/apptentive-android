/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;
/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;

import com.apptentive.android.sdk.Apptentive;

/**
 * All Apptentive internal activities extend this class instead of Activity to be able to use addroid support.v7 library.
 * + *
 * + * @author Barry Li
 * +
 */
@SuppressLint("Registered")
public class ApptentiveInternalActivity extends AppCompatActivity {

	@Override
	protected void onStart() {
		super.onStart();
		Apptentive.onStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Apptentive.onStop(this);
	}
}
