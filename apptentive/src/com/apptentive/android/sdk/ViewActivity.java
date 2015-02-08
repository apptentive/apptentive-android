/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
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
import android.widget.Toast;
import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.interaction.model.*;
import com.apptentive.android.sdk.module.engagement.interaction.view.*;
import com.apptentive.android.sdk.module.engagement.interaction.view.survey.SurveyInteractionView;
import com.apptentive.android.sdk.module.messagecenter.ApptentiveMessageCenter;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterView;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.ActivityUtil;
import com.apptentive.android.sdk.util.Constants;
import org.json.JSONObject;

/**
 * For internal use only. Used to launch Apptentive Message Center, Survey, and About views.
 *
 * @author Sky Kelsey
 */
public class ViewActivity extends ApptentiveActivity {

	private ActivityContent activityContent;
	private ActivityContent.Type activeContentType;


	/**
	 * The class name to return to if this Activity was launched from a push notification.
	 */
	private String pushCallbackActivityName;

	/**
	 * A flag that is set when this Activity was launched from a push notification. If true, we should launch
	 * pushCallbackActivityName when we finish this Activity.
	 */
	private boolean returnToPushCallbackActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			requestWindowFeature(Window.FEATURE_NO_TITLE);

			String activityContentTypeString = getIntent().getStringExtra(ActivityContent.KEY);
			String parseStringExtra = getIntent().getStringExtra("com.parse.Data");

			if (activityContentTypeString != null) {
				Log.v("Started ViewActivity normally for %s.", activityContent);
				activeContentType = ActivityContent.Type.parse(activityContentTypeString);

				try {
					switch (activeContentType) {
						case ABOUT:
							break;
						case MESSAGE_CENTER:
							break;
						case INTERACTION:
							String interactionString = getIntent().getExtras().getCharSequence(Interaction.KEY_NAME).toString();
							Interaction interaction = Interaction.Factory.parseInteraction(interactionString);
							switch (interaction.getType()) {
								case UpgradeMessage:
									activityContent = new UpgradeMessageInteractionView((UpgradeMessageInteraction) interaction);
									break;
								case EnjoymentDialog:
									activityContent = new EnjoymentDialogInteractionView((EnjoymentDialogInteraction) interaction);
									break;
								case RatingDialog:
									activityContent = new RatingDialogInteractionView((RatingDialogInteraction) interaction);
									break;
								case AppStoreRating:
									activityContent = new AppStoreRatingInteractionView((AppStoreRatingInteraction) interaction);
									break;
								case FeedbackDialog:
									activityContent = new FeedbackDialogInteractionView((FeedbackDialogInteraction) interaction);
									break;
								case Survey:
									activityContent = new SurveyInteractionView((SurveyInteraction) interaction);
									break;
								case MessageCenter:
									// For now, we use the old method.
									getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
									finish();
									Apptentive.showMessageCenter(this);
									return;
								case TextModal:
									activityContent = new TextModalInteractionView((TextModalInteraction) interaction);
									break;
								case NavigateToLink:
									activityContent = new NavigateToLinkInteractionView((NavigateToLinkInteraction) interaction);
									break;
								default:
									break;
							}
							if (activityContent == null) {
								finish();
							} else {
								activityContent.onCreate(this, savedInstanceState);
							}
							break;
						default:
							Log.w("No Activity specified. Finishing...");
							finish();
							break;
					}
				} catch (Exception e) {
					Log.e("Error starting ViewActivity.", e);
					MetricModule.sendError(this, e, null, null);
				}
			} else
				// If no content was sent to this Activity, then it may have been started from a Parse push notification.
				if (parseStringExtra != null) {
					Log.i("Started ViewActivity from Parse push.");

					// Save off the callback Activity if one was passed in.
					pushCallbackActivityName = ApptentiveInternal.getPushCallbackActivityName();

					// If the callback is null and we got here, then the developer forgot to set it.
					if (pushCallbackActivityName != null) {
						returnToPushCallbackActivity = true;
						JSONObject parseJson = new JSONObject(parseStringExtra);
						String apptentiveStringData = parseJson.optString(Apptentive.APPTENTIVE_PUSH_EXTRA_KEY);
						JSONObject apptentiveJson = new JSONObject(apptentiveStringData);
						ApptentiveInternal.PushAction action = ApptentiveInternal.PushAction.parse(apptentiveJson.getString(ApptentiveInternal.PUSH_ACTION));
						switch (action) {
							case pmc:
								activeContentType = ActivityContent.Type.MESSAGE_CENTER;
								break;
							default:
								break;
						}
					} else {
						Log.a("Push callback Activity was not set. Make sure to call Apptentive.setPushCallback()");
						if (GlobalInfo.isAppDebuggable) {
							Toast.makeText(this, "Push callback Activity was not set. Make sure to call Apptentive.setPushCallback()", Toast.LENGTH_LONG).show();
						}
						finish();
					}
				} else {
					Log.e("Started ViewActivity in a bad way.");
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
		super.onStart();
		switch (activeContentType) {
			case ABOUT:
				AboutModule.getInstance().doShow(this);
				break;
			case MESSAGE_CENTER:
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
				ApptentiveMessageCenter.doShow(this);
				break;
			case INTERACTION:
				// Interactions are already set up from onCreate().
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
			case MESSAGE_CENTER:
				ApptentiveMessageCenter.onStop(this);
				break;
			case INTERACTION:
				// Interactions don't need to hear about onStop().
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
			case MESSAGE_CENTER:
				finish = ApptentiveMessageCenter.onBackPressed(this);
				break;
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
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("returnToPushCallbackActivity", returnToPushCallbackActivity);
		outState.putString("pushCallbackActivityName", pushCallbackActivityName);
		if(activityContent != null) {
			activityContent.onSaveInstanceState(outState);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		returnToPushCallbackActivity = savedInstanceState.getBoolean("returnToPushCallbackActivity", false);
		pushCallbackActivityName = savedInstanceState.getString("pushCallbackActivityName");
		if(activityContent != null) {
			activityContent.onRestoreInstanceState(savedInstanceState);
		}
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, R.anim.slide_down_out);
		// If we started from a Parse push, then we need to return to the callback Activity when this Activity finishes.
		if (pushCallbackActivityName != null) {
			Log.d("Returning to callback Activity: %s", pushCallbackActivityName);
			Class<? extends Activity> cls = ActivityUtil.getActivityClassFromName(pushCallbackActivityName);
			if (cls != null) {
				Intent intent = new Intent(this, cls);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_up_in, 0);
			}
		}
	}
}
