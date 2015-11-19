/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;

import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.*;
import com.apptentive.android.sdk.module.engagement.interaction.view.*;
import com.apptentive.android.sdk.module.engagement.interaction.view.survey.SurveyInteractionView;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterActivityContent;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterErrorActivityContent;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Util;

/**
 * For internal use only. Used to launch Apptentive Message Center, Survey, and About views.
 *
 * @author Sky Kelsey
 */
public class ViewActivity extends ApptentiveInternalActivity implements ActivityCompat.OnRequestPermissionsResultCallback {


	private ActivityContent activityContent;
	private ActivityContent.Type activeContentType;

	private boolean activityExtraBoolean;


	public ActivityContent getActivityContent() {
		return activityContent;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try {

			String activityContentTypeString = getIntent().getStringExtra(ActivityContent.KEY);
			Interaction interaction = null;
			if (activityContentTypeString != null) {
				Log.v("Started ViewActivity normally for %s.", activityContent);
				activeContentType = ActivityContent.Type.parse(activityContentTypeString);
				/* ViewActivity must have a theme inherit from Apptentive Base themes.
				*  If hosting app fails to specify one in AndroidManifest, apply a default ApptentiveTheme.
				*  The check is done through checking if one of the apptentive attributes have value defined
				*/
				if (Util.getThemeColor(this, R.attr.apptentive_material_toolbar_foreground) == 0) {
					setTheme(R.style.ApptentiveTheme);
				}
				if (activeContentType == ActivityContent.Type.ABOUT) {
					setTheme(R.style.ApptentiveTheme_About);
				} else if (activeContentType == ActivityContent.Type.INTERACTION) {
					String interactionString;
					if (savedInstanceState != null) {
						interactionString = savedInstanceState.getString(Interaction.JSON_STRING);
					} else {
						interactionString = getIntent().getExtras().getCharSequence(Interaction.KEY_NAME).toString();
					}
					interaction = Interaction.Factory.parseInteraction(interactionString);
					if (interaction != null) {
						switch (interaction.getType()) {
							case UpgradeMessage:
								activityContent = new UpgradeMessageInteractionView((UpgradeMessageInteraction) interaction);
								setTheme(R.style.ApptentiveTheme_Transparent);
								break;
							case EnjoymentDialog:
								activityContent = new EnjoymentDialogInteractionView((EnjoymentDialogInteraction) interaction);
								setTheme(R.style.ApptentiveTheme_Transparent);
								break;
							case RatingDialog:
								activityContent = new RatingDialogInteractionView((RatingDialogInteraction) interaction);
								setTheme(R.style.ApptentiveTheme_Transparent);
								break;
							case AppStoreRating:
								activityContent = new AppStoreRatingInteractionView((AppStoreRatingInteraction) interaction);
								setTheme(R.style.ApptentiveTheme_Transparent);
								break;
							case Survey:
								activityContent = new SurveyInteractionView((SurveyInteraction) interaction);
								setTheme(R.style.ApptentiveTheme_Transparent);
								break;
							case MessageCenter:
								activityContent = new MessageCenterActivityContent((MessageCenterInteraction) interaction);
								break;
							case TextModal:
								activityContent = new TextModalInteractionView((TextModalInteraction) interaction);
								setTheme(R.style.ApptentiveTheme_Transparent);
								break;
							case NavigateToLink:
								activityContent = new NavigateToLinkInteractionView((NavigateToLinkInteraction) interaction);
								setTheme(R.style.ApptentiveTheme_Transparent);
								break;
							default:
								break;
						}
					}
				}

				boolean activityContentRequired = true;

				super.onCreate(savedInstanceState);

				try {
					switch (activeContentType) {
						case ENGAGE_INTERNAL_EVENT:
							String eventName = getIntent().getStringExtra(ActivityContent.EVENT_NAME);
							if (eventName != null) {
								EngagementModule.engageInternal(this, eventName);
							}
							break;
						case ABOUT:
							activityContentRequired = false;
							activityExtraBoolean = getIntent().getBooleanExtra(ActivityContent.EXTRA, true);
							break;
						case MESSAGE_CENTER_ERROR:
							activityContent = new MessageCenterErrorActivityContent();
							break;
						case INTERACTION:
							break; // end INTERACTION
						default:
							Log.w("No Activity specified. Finishing...");
							break;
					}
					if (activityContentRequired) {
						if (activityContent == null) {
							finish();
						} else {
							activityContent.onCreate(this, savedInstanceState);
						}
					}
				} catch (Exception e) {
					Log.e("Error starting ViewActivity.", e);
					MetricModule.sendError(this, e, null, null);
				}
			}
		} catch (Exception e) {
			Log.e("Error creating ViewActivity.", e);
			MetricModule.sendError(this, e, null, null);
		}
		if (activeContentType == null) {
			finish();
		}
		Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
		window.addFlags(WindowManager.LayoutParams.FLAG_DITHER);
	}

	@Override
	protected void onStart() {
		switch (activeContentType) {
			case ABOUT:
				super.onStart();
				AboutModule.getInstance().setupView(this, activityExtraBoolean);
				break;
			case MESSAGE_CENTER_ERROR:
				super.onStart();
				break;
			case INTERACTION:
				activityContent.onStart();
				super.onStart();
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
			case MESSAGE_CENTER_ERROR:
				break;
			case INTERACTION:
				activityContent.onStop();
				break;
			default:
				break;
		}
	}

	@Override
	public void onBackPressed() {
		boolean finish = true;
		switch (activeContentType) {
			case ABOUT:
				finish = AboutModule.getInstance().onBackPressed(this);
				break;
			case MESSAGE_CENTER_ERROR:
			case INTERACTION:
				if (activityContent != null) {
					finish = activityContent.onBackPressed(this);
				}
				break;
			default:
				break;
		}

		if (finish) {
			finish();
			super.onBackPressed();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (activeContentType) {
			case ABOUT:
				break;
			case MESSAGE_CENTER_ERROR:
				break;
			case INTERACTION:
				activityContent.onActivityResult(requestCode, resultCode, data);
				break;
			default:
				break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (activityContent != null) {
			activityContent.onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (activityContent != null) {
			activityContent.onPause();
		}
	}

	public void showAboutActivity(View view) {
		AboutModule.getInstance().show(this, true);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (activityContent != null) {
			activityContent.onSaveInstanceState(outState);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (activityContent != null) {
			activityContent.onRestoreInstanceState(savedInstanceState);
		}
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, R.anim.slide_down_out);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		overridePendingTransition(R.anim.slide_up_in, 0);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
																				 @NonNull int[] grantResults) {
		switch (activeContentType) {
			case ABOUT:
				break;
			case MESSAGE_CENTER_ERROR:
				break;
			case INTERACTION:
				activityContent.onRequestPermissionsResult(requestCode, permissions, grantResults);
				break;
			default:
				break;
		}
	}
}
