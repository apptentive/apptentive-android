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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.UpgradeMessageInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.view.UpgradeMessageInteractionView;
import com.apptentive.android.sdk.module.messagecenter.ApptentiveMessageCenter;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterView;
import com.apptentive.android.sdk.util.Constants;

/**
 * For internal use only. Used to launch Apptentive Message Center, Survey, and About views.
 * @author Sky Kelsey
 */
public class ViewActivity extends ApptentiveActivity {

	private ActivityContent activityContent;
	private ActivityContent.Type activeContentType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		activeContentType = ActivityContent.Type.parse(getIntent().getStringExtra(ActivityContent.KEY));

		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
	}

	@Override
	protected void onStart() {
		super.onStart();
		switch (activeContentType) {
			case ABOUT:
				AboutModule.getInstance().doShow(this);
				break;
			case SURVEY:
				SurveyModule.getInstance().doShow(this);
				break;
			case MESSAGE_CENTER:
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
				ApptentiveMessageCenter.doShow(this);
				break;
			case INTERACTION:
				String interactionString = getIntent().getExtras().getCharSequence(Interaction.KEY_NAME).toString();
				Interaction interaction = Interaction.Factory.parseInteraction(interactionString);
				UpgradeMessageInteractionView view = null;
				switch (interaction.getType()) {
					case UpgradeMessage:
						view = new UpgradeMessageInteractionView((UpgradeMessageInteraction) interaction);
						break;
					default:
						break;
				}
				activityContent = view;
				view.show(this);
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
		switch (activeContentType) {
			case ABOUT:
				break;
			case SURVEY:
				break;
			case MESSAGE_CENTER:
				ApptentiveMessageCenter.onStop(this);
				break;
			case INTERACTION:
				if (activityContent != null) {
					activityContent.onStop();
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void onBackPressed() {
		switch(activeContentType) {
			case ABOUT:
				AboutModule.getInstance().onBackPressed(this);
				break;
			case SURVEY:
				SurveyModule.getInstance().onBackPressed(this);
				break;
			case MESSAGE_CENTER:
				ApptentiveMessageCenter.onBackPressed(this);
				break;
			case INTERACTION:
				if (activityContent != null) {
					activityContent.onBackPressed();
				}
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

	public void showAboutActivity(View view) {
		AboutModule.getInstance().show(this);
	}
}
