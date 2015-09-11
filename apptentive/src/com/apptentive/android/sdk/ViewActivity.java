/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.*;
import com.apptentive.android.sdk.module.engagement.interaction.view.*;
import com.apptentive.android.sdk.module.engagement.interaction.view.survey.SurveyInteractionView;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterActivityContent;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterErrorActivityContent;
import com.apptentive.android.sdk.module.metric.MetricModule;

/**
 * For internal use only. Used to launch Apptentive Message Center, Survey, and About views.
 *
 * @author Sky Kelsey
 */
public class ViewActivity extends ApptentiveActivity {


	private ActivityContent activityContent;
	private ActivityContent.Type activeContentType;

	private boolean activityExtraBoolean;

	// Use AppCompatDelegate istead of extending AppCompatActivity
	private AppCompatDelegate appCompatDelegate;

	private AppCompatDelegate getDelegate() {
		if (appCompatDelegate == null) {
			appCompatDelegate = AppCompatDelegate.create(this, null);
		}
		return appCompatDelegate;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getDelegate().onPostCreate(savedInstanceState);
	}

	public ActionBar getSupportActionBar() {
		return getDelegate().getSupportActionBar();
	}

	public void setSupportActionBar(@Nullable Toolbar toolbar) {
		getDelegate().setSupportActionBar(toolbar);
	}

	@Override
	public MenuInflater getMenuInflater() {
		return getDelegate().getMenuInflater();
	}

	@Override
	public void setContentView(int layoutResID) {
		getDelegate().setContentView(layoutResID);
	}

	@Override
	public void setContentView(View view) {
		getDelegate().setContentView(view);
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		getDelegate().setContentView(view, params);
	}

	@Override
	public void addContentView(View view, ViewGroup.LayoutParams params) {
		getDelegate().addContentView(view, params);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		getDelegate().onPostResume();
	}

	@Override
	protected void onTitleChanged(CharSequence title, int color) {
		super.onTitleChanged(title, color);
		getDelegate().setTitle(title);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		getDelegate().onConfigurationChanged(newConfig);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getDelegate().onDestroy();
	}

	public void invalidateOptionsMenu() {
		getDelegate().invalidateOptionsMenu();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try {

			String activityContentTypeString = getIntent().getStringExtra(ActivityContent.KEY);
			Interaction interaction = null;
			if (activityContentTypeString != null) {
				Log.v("Started ViewActivity normally for %s.", activityContent);
				activeContentType = ActivityContent.Type.parse(activityContentTypeString);
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
				getDelegate().installViewFactory();
				getDelegate().onCreate(savedInstanceState);

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
		getDelegate().onStop();
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
}
