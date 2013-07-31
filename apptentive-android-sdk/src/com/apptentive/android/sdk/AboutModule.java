/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;
import com.apptentive.android.sdk.util.Constants;

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

	public void show(Activity activity) {
		Intent intent = new Intent();
		intent.setClass(activity, ViewActivity.class);
		intent.putExtra("module", ViewActivity.Module.ABOUT.toString());
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_up_in, R.anim.slide_down_out);
	}

	void doShow(final Activity activity) {
		activity.setContentView(R.layout.apptentive_about);

		TextView information = (TextView) activity.findViewById(R.id.apptentive_about_link);
		information.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.apptentive.com"));
				activity.startActivity(browserIntent);
			}
		});

		TextView privacy = (TextView) activity.findViewById(R.id.apptentive_about_privacy_link);
		privacy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.apptentive.com/privacy"));
				activity.startActivity(browserIntent);
			}
		});

		TextView version = (TextView) activity.findViewById(R.id.version);
		String versionString = activity.getResources().getString(R.string.apptentive_version);
		version.setText(String.format(versionString, Constants.APPTENTIVE_SDK_VERSION));
	}

	void onBackPressed(Activity activity) {
		activity.finish();
		activity.overridePendingTransition(R.anim.slide_up_in, R.anim.slide_down_out);
	}

}
