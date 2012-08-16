/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;

/**
 * For internal use only. Used to launch Apptentive feedback and ratings views.
 * @author Sky Kelsey
 */
public class ViewActivity extends ApptentiveActivity {

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
