/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.impl.GooglePlayRatingProvider;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;
import com.apptentive.android.sdk.util.Constants;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains only internal methods. These methods should not be access directly by the host app.
 *
 * @author Sky Kelsey
 */
public class ApptentiveInternal {

	private static IRatingProvider ratingProvider;
	private static Map<String, String> ratingProviderArgs;
	private static WeakReference<OnSurveyFinishedListener> onSurveyFinishedListener;

	public static final int NOTIFICATION_MESSAGE = 0;
	public static final int NOTIFICATION_RATE = 1;
	public static final int NOTIFICATION_SURVEY = 2;


	public static final String PUSH_ACTION = "action";

	public static enum PushAction {
		pmc,       // Present Message Center.
		unknown;   // Anything unknown will not be handled.

		public static PushAction parse(String name) {
			try {
				return PushAction.valueOf(name);
			} catch (IllegalArgumentException e) {
				Log.d("Error parsing unknown PushAction: " + name);
			}
			return unknown;
		}
	}

	public static void onAppLaunch(final Activity activity) {
		EngagementModule.engageInternal(activity, Event.EventLabel.app__launch.getLabelName());
	}

	public static IRatingProvider getRatingProvider() {
		if (ratingProvider == null) {
			ratingProvider = new GooglePlayRatingProvider();
		}
		return ratingProvider;
	}

	public static void setRatingProvider(IRatingProvider ratingProvider) {
		ApptentiveInternal.ratingProvider = ratingProvider;
	}

	public static Map<String, String> getRatingProviderArgs() {
		return ratingProviderArgs;
	}

	public static void putRatingProviderArg(String key, String value) {
		if (ratingProviderArgs == null) {
			ratingProviderArgs = new HashMap<String, String>();
		}
		ratingProviderArgs.put(key, value);
	}

	public static void setOnSurveyFinishedListener(OnSurveyFinishedListener onSurveyFinishedListener) {
		if (onSurveyFinishedListener != null) {
			ApptentiveInternal.onSurveyFinishedListener = new WeakReference<OnSurveyFinishedListener>(onSurveyFinishedListener);
		} else {
			ApptentiveInternal.onSurveyFinishedListener = null;
		}
	}

	public static OnSurveyFinishedListener getOnSurveyFinishedListener() {
		return (onSurveyFinishedListener == null)? null : onSurveyFinishedListener.get();
	}

	/**
	 * Pass in a log level to override the default, which is {@link Log.Level#INFO}
	 *
	 */
	public static void setMinimumLogLevel(Log.Level level) {
		Log.overrideLogLevel(level);
	}

	private static String pushCallbackActivityName;
	public static void setPushCallbackActivity(Class<? extends Activity> activity) {
		pushCallbackActivityName = activity.getName();
		Log.d("Setting push callback activity name to %s", pushCallbackActivityName);
	}

	public static String getPushCallbackActivityName() {
		return pushCallbackActivityName;
	}

	/**
	 * The key that is used to store extra data on an Apptentive push notification.
	 */
	static final String APPTENTIVE_PUSH_EXTRA_KEY = "apptentive";

	static final String PARSE_PUSH_EXTRA_KEY = "com.parse.Data";

	static String getApptentivePushNotificationData(Intent intent) {
		String apptentive = null;
		if (intent != null) {
			Log.v("Got an Intent.");
			// Parse
			if (intent.hasExtra(PARSE_PUSH_EXTRA_KEY)) {
				String parseStringExtra = intent.getStringExtra(PARSE_PUSH_EXTRA_KEY);
				Log.v("Got a Parse Push.");
				try {
					JSONObject parseJson = new JSONObject(parseStringExtra);
					apptentive = parseJson.optString(APPTENTIVE_PUSH_EXTRA_KEY, null);
				} catch (JSONException e) {
					Log.e("Corrupt Parse String Extra: %s", parseStringExtra);
				}
			} else {
				// Straight GCM / SNS
				Log.v("Got a non-Parse push.");
				apptentive = intent.getStringExtra(APPTENTIVE_PUSH_EXTRA_KEY);
			}
		}
		return apptentive;
	}

	static String getApptentivePushNotificationData(Bundle pushBundle) {
		if (pushBundle != null) {
			return pushBundle.getString(APPTENTIVE_PUSH_EXTRA_KEY);
		}
		return null;
	}

	static boolean setPendingPushNotification(Context context, String apptentivePushData) {
		if (apptentivePushData != null) {
			Log.d("Saving Apptentive push notification data.");
			SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
			prefs.edit().putString(Constants.PREF_KEY_PENDING_PUSH_NOTIFICATION, apptentivePushData).commit();
			return true;
		}
		return false;
	}

	private static LinearLayout notificationContainer;
	private static ViewGroup parentView;
	private static Handler uiHandler;

	private static String[] notificationTitle = {"Message", "Rate", "Survey"};
	private static int[] notificationImage = {R.drawable.icon_message, R.drawable.icon_heart, R.drawable.icon_survey};
	private static int[] notificationColor = new int[]{0xffff0033, 0xff66cccc, 0xffffcc33};


	static void addInAppNotification(final Activity activity, int category, String message) {
		final WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
		wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		wmParams.format = PixelFormat.RGBA_8888;
		wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

		wmParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
		wmParams.width = ActionBar.LayoutParams.WRAP_CONTENT;
		wmParams.height = ActionBar.LayoutParams.WRAP_CONTENT;

		// Initialise container view first in which every notification is to be
		// added.
		if (notificationContainer == null) {

			notificationContainer = (LinearLayout) activity.getLayoutInflater().inflate(
					R.layout.apptentive_notification_main_layout, null);

			parentView = new FrameLayout(activity);
			wm.addView(parentView, wmParams);
			parentView.addView(notificationContainer);
			// Add that container to the window manager
			//wm.updateViewLayout(notificationContainerView, lp);
		}

		if (uiHandler == null) {
			uiHandler = new Handler();
		}

		View viewItem = null;

		// find the existing notification and replace it
		for (int i = 0; i< notificationContainer.getChildCount(); i++) {
			View child = notificationContainer.getChildAt(i);
			if (child.getId() == category) {
				viewItem = child;
				Runnable r = (Runnable) viewItem.getTag();
				if (r != null) {
					uiHandler.removeCallbacks(r);
				}
				break;
			}
		}
		// create new notification is not existing yet
		if (viewItem == null) {
			viewItem = activity.getLayoutInflater().inflate(
					R.layout.apptentive_notification_item_view, null);
			viewItem.setId(category);
			viewItem.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int type = v.getId();
					Runnable r = (Runnable) v.getTag();
					if (r != null) {
						uiHandler.removeCallbacks(r);
					}

					if (notificationContainer.getChildCount() == 1) {
						wm.removeView(parentView);
					}

					notificationContainer.removeView(v);
					switch(type) {
						case NOTIFICATION_MESSAGE:
							wm.removeView(parentView);
							notificationContainer.removeAllViews();
							Apptentive.showMessageCenter(activity);
							break;
						case NOTIFICATION_RATE:
						case NOTIFICATION_SURVEY:

							break;
					}
				}
			});
			viewItem.setBackgroundColor(notificationColor[category]);
			ImageView catImage = (ImageView) viewItem
					.findViewById(R.id.notification_image);
			catImage.setImageResource(notificationImage[category]);

			TextView titleTextView = (TextView) viewItem
					.findViewById(R.id.notification_title);
			titleTextView.setText(notificationTitle[category]);
			notificationContainer.addView(viewItem, 0);
		}


		// update notification body text
		TextView messageTextView = (TextView) viewItem
				.findViewById(R.id.notification_text);
		messageTextView.setText(message);


		//view.measure(View.MeasureSpec.makeMeasureSpec(notificationContainer.getWidth(), View.MeasureSpec.EXACTLY),
		//		View.MeasureSpec.makeMeasureSpec(120, View.MeasureSpec.AT_MOST));
		LinearLayout.LayoutParams childLP = new LinearLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				100);

		notificationContainer.updateViewLayout(viewItem, childLP);


		final View finalview = viewItem;
		Runnable r = new Runnable() {
			@Override
			public void run() {
				if (notificationContainer.getChildCount() == 1) {
					final Animation out = AnimationUtils.loadAnimation(activity, R.anim.slide_down_out);
					out.setDuration(500);
					out.setAnimationListener(new Animation.AnimationListener() {
						public void onAnimationStart(Animation animation) {
						}

						public void onAnimationRepeat(Animation animation) {
						}

						public void onAnimationEnd(Animation animation) {
							notificationContainer.removeView(finalview);
							wm.removeView(parentView);
						}
					});
					finalview.startAnimation(out);
				} else {
					//if (!isFinishing())
					notificationContainer.removeView(finalview);
				}
			}
		};
		viewItem.setTag(r);
        uiHandler.postDelayed(r, 5000);

		try {
			wm.updateViewLayout(parentView, wmParams);
		} catch (Exception e) {
			wm.addView(parentView, wmParams);
			wm.updateViewLayout(parentView, wmParams);
		}

		if (notificationContainer.getChildCount() == 1) {
			showNotificationEnterAnimation(activity, viewItem);
		}

	}

	static void showNotificationEnterAnimation(Context context, View view) {
		final Animation in = AnimationUtils.loadAnimation(context, R.anim.slide_up_in);
		in.setDuration(500);
		in.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
			}
		});
		view.startAnimation(in);
	}

	public static void showMessageCenterInternal(Activity activity, Map<String, String> customData) {
		Intent intent = new Intent();
		intent.setClass(activity, ViewActivity.class);
		intent.putExtra(ActivityContent.KEY, ActivityContent.Type.MESSAGE_CENTER.toString());
		intent.putExtra(ActivityContent.EXTRA, (customData instanceof Serializable) ? (Serializable) customData : null);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_up_in, R.anim.slide_down_out);
	}

}

