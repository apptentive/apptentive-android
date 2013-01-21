/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

/**
 * @author Sky Kelsey
 */
public class AboutModule {

	// *************************************************************************************************
	// ********************************************* Static ********************************************
	// *************************************************************************************************
	private static AboutModule instance = null;

	public static AboutModule getInstance() {
		if (instance == null) {
			instance = new AboutModule();
		}
		return instance;
	}

	// *************************************************************************************************
	// ********************************************* Private *******************************************
	// *************************************************************************************************

	private AboutModule() {
	}

	// *************************************************************************************************
	// ******************************************* Not Private *****************************************
	// *************************************************************************************************

	public void show(Context context) {
		Intent intent = new Intent();
		intent.setClass(context, ViewActivity.class);
		intent.putExtra("module", ViewActivity.Module.ABOUT.toString());
		context.startActivity(intent);
	}

	void doShow(final Activity activity) {
		TextView links = (TextView) activity.findViewById(R.id.apptentive_about_links);
		links.setMovementMethod(LinkMovementMethod.getInstance());
	}
}
