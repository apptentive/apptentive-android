/*
 * ApptentiveActivity.java
 *
 * Created by skelsey on 2011-09-18.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import com.apptentive.android.sdk.model.FeedbackController;

public class ApptentiveActivity  extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.apptentive_feedback);
		new FeedbackController(this, getIntent().getBooleanExtra("forced", false));
	}
}
