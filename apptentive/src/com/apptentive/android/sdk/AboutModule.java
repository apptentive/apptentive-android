/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.apptentive.android.sdk.model.ExtendedData;
import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.util.Constants;

/**
 * @author Sky Kelsey
 */
public class AboutModule {

	private static final String INTERACTION_NAME = "About";

	private static final String EVENT_NAME_LAUNCH = "launch";
	private static final String EVENT_NAME_CLOSE = "close";
	private static final String EVENT_NAME_CANCEL = "cancel";

	private static AboutModule instance = null;

	public static AboutModule getInstance() {
		if (instance == null) {
			instance = new AboutModule();
		}
		return instance;
	}

	private AboutModule() {
	}

	/**
	 * Show About
	 *
	 * @param activity The launching activity
	 * @param showBrandingBand  If true, show branding band on About page
	 */
	public void show(Activity activity, boolean showBrandingBand) {
		EngagementModule.engage(activity, "com.apptentive", INTERACTION_NAME, null, EVENT_NAME_LAUNCH, null, null, (ExtendedData[]) null);
		Intent intent = new Intent();
		intent.setClass(activity, ViewActivity.class);
		intent.putExtra(ActivityContent.KEY, ActivityContent.Type.ABOUT.toString());
		intent.putExtra(ActivityContent.EXTRA, showBrandingBand);
		activity.startActivity(intent);
	}

	void setupView(final Activity activity, boolean showBrandingBand) {
		activity.setContentView(R.layout.apptentive_about);

		final String packageName = activity.getPackageName();

		if (!showBrandingBand) {
			activity.findViewById(R.id.apptentive_branding_view).setVisibility(View.GONE);
		} else {
			activity.findViewById(R.id.apptentive_branding_view).setClickable(false); // Don't let the about view launch itself.
		}

		View close = activity.findViewById(R.id.close_about);
		close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EngagementModule.engage(activity, "com.apptentive", INTERACTION_NAME, null, EVENT_NAME_CLOSE, null, null, (ExtendedData[]) null);
				activity.finish();
			}
		});

		TextView information = (TextView) activity.findViewById(R.id.about_description_link);
		information.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("http://www.apptentive.com/?source=%s", packageName)));
				activity.startActivity(browserIntent);
			}
		});

		TextView privacy = (TextView) activity.findViewById(R.id.privacy_link);
		privacy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("http://www.apptentive.com/privacy/?source=%s", packageName)));
				activity.startActivity(browserIntent);
			}
		});

	}

	boolean onBackPressed(Activity activity) {
		EngagementModule.engage(activity, "com.apptentive", INTERACTION_NAME, null, EVENT_NAME_CANCEL, null, null, (ExtendedData[]) null);
		return true;
	}
}
