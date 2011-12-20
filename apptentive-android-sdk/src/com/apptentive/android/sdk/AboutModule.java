/*
 * Created by Sky Kelsey on 2011-12-18.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */
package com.apptentive.android.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

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
		intent.setClass(context, ApptentiveActivity.class);
		intent.putExtra("module", ApptentiveActivity.Module.ABOUT.toString());
		context.startActivity(intent);
		return;
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
		activity.findViewById(R.id.apptentive_button_about_okay);
		activity.findViewById(R.id.apptentive_button_about_okay).setOnClickListener(clickListener);
	}
}
