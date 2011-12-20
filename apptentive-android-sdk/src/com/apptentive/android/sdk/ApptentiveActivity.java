/*
 * Created by Sky Kelsey on 2011-09-18.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;

public class ApptentiveActivity extends Activity {

	private Module activeModule;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		activeModule = Module.valueOf(getIntent().getStringExtra("module"));

		switch (activeModule) {
			case ABOUT:
				setContentView(R.layout.apptentive_about);
				AboutModule.getInstance().doShow(this);
				break;
			case SURVEY:
				setContentView(R.layout.apptentive_activity);
				LayoutInflater inflater = getLayoutInflater();
				ViewGroup contentView = (ViewGroup) findViewById(R.id.apptentive_activity_content_view);
				contentView.removeAllViews();

				inflater.inflate(R.layout.apptentive_survey, contentView);

				SurveyModule.getInstance().doShow(this);
				break;
			default:
				Log.w("No Activity specified. Finishing...");
				finish();
				break;
		}
	}

	public static enum Module {
		ABOUT,
		SURVEY
	}
}
