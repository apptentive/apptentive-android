/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import com.apptentive.android.sdk.module.messagecenter.ApptentiveMessageCenter;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterView;
import com.apptentive.android.sdk.util.Constants;

/**
 * For internal use only. Used to launch Apptentive Message Center, Survey, and About views.
 * @author Sky Kelsey
 */
public class ViewActivity extends ApptentiveActivity {

	private Module activeModule;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		activeModule = Module.valueOf(getIntent().getStringExtra("module"));

		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
	}

	@Override
	protected void onStart() {
		super.onStart();
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

				setContentView(R.layout.apptentive_survey_dialog);

				SurveyModule.getInstance().doShow(this);
				break;
			case MESSAGE_CENTER:
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
				ApptentiveMessageCenter.doShow(this);
				break;
			default:
				Log.w("No Activity specified. Finishing...");
				finish();
				break;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		switch (activeModule) {
			case ABOUT:
				break;
			case SURVEY:
				break;
			case MESSAGE_CENTER:
				ApptentiveMessageCenter.onStop(this);
				break;
			default:
				break;
		}
	}

	@Override
	public void onBackPressed() {
		switch(activeModule) {
			case ABOUT:
				break;
			case SURVEY:
				SurveyModule.getInstance().onBackPressed(this);
				break;
			case MESSAGE_CENTER:
				ApptentiveMessageCenter.onBackPressed(this);
				break;
			default:
				break;
		}
		super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK) {
			switch(requestCode) {
				case Constants.REQUEST_CODE_PHOTO_FROM_MESSAGE_CENTER:
					MessageCenterView.showAttachmentDialog(this, data.getData());
					break;
				default:
					break;
			}
		}
	}

	public static enum Module {
		ABOUT,
		SURVEY,
		MESSAGE_CENTER
	}
}
