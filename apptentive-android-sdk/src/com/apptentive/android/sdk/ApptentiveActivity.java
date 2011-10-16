/*
 * ApptentiveActivity.java
 *
 * Created by Sky Kelsey on 2011-09-18.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import com.apptentive.android.sdk.activity.BaseActivity;
import com.apptentive.android.sdk.model.ApptentiveModel;
import com.apptentive.android.sdk.model.FeedbackController;
import com.apptentive.android.sdk.model.ViewController;
import com.apptentive.android.sdk.survey.SurveyController;
import com.apptentive.android.sdk.util.Util;

public class ApptentiveActivity  extends BaseActivity {

	private ALog log = new ALog(this.getClass());

	private Module activeModule;

	private ViewController controller;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		activeModule = Module.valueOf(getIntent().getStringExtra("module"));

		setContentView(R.layout.apptentive_activity);
		LayoutInflater inflater = getLayoutInflater();
		ViewGroup contentView = (ViewGroup) findViewById(R.id.apptentive_activity_content_view);
		contentView.removeAllViews();

		// Inflate the wrapper view, and then inflate the content view into it
		switch(activeModule){
			case FEEDBACK:
				inflater.inflate(R.layout.apptentive_feedback, contentView);
				controller = new FeedbackController(this, getIntent().getBooleanExtra("forced", false));
				break;
			case SURVEY:
				ApptentiveModel model = ApptentiveModel.getInstance();
				if(model.getSurveys() == null || model.getSurveys().size() == 0){
					finish();
					return;
				}
				inflater.inflate(R.layout.apptentive_survey, contentView);
				controller = new SurveyController(this);
				break;
			default:
				inflater.inflate(R.layout.apptentive_feedback, contentView);
				controller = new FeedbackController(this, getIntent().getBooleanExtra("forced", false));
				break;
		}
	}

	@Override
	protected void onDestroy() {
		controller.cleanup();
		controller = null;
		super.onDestroy();
	}

	public static enum Module{
		FEEDBACK,
		SURVEY
	}
}
