/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

/**
 * @author Sky Kelsey
 */
public class AboutModule {

	// *************************************************************************************************
	// ********************************************* Static ********************************************
	// *************************************************************************************************
	private static AboutModule instance = null;

	static AboutModule getInstance() {
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

	void doShow(final Activity activity){
		View.OnClickListener clickListener = new View.OnClickListener() {
			public void onClick(View view) {
				int id = view.getId();
				if(id == R.id.apptentive_button_about_okay){
					activity.finish();
				}
			}
		};
		activity.findViewById(R.id.apptentive_button_about_okay).setOnClickListener(clickListener);
	}
}
