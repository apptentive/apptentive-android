/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.MessageCenterInteraction;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.impl.GooglePlayRatingProvider;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;
import com.apptentive.android.sdk.util.Constants;
import org.json.JSONException;
import org.json.JSONObject;

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

	// Used for temporarily holding customData that needs to be sent on the next message the consumer sends.
	private static Map<String, String> customData;

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

	public static boolean showMessageCenterInternal(Activity activity, Map<String, String> customData) {
		boolean interactionShown = false;
		if (EngagementModule.canShowInteraction(activity, "com.apptentive", "app", MessageCenterInteraction.DEFAULT_INTERNAL_EVENT_NAME)) {
			ApptentiveInternal.customData = customData;
			interactionShown = EngagementModule.engageInternal(activity, MessageCenterInteraction.DEFAULT_INTERNAL_EVENT_NAME);
			if (!interactionShown) {
				ApptentiveInternal.customData = null;
			}
		} else {
			showMessageCenterFallback(activity);
		}
		return interactionShown;
	}

	public static void showMessageCenterFallback(Activity activity) {
		Intent intent = MessageCenterInteraction.generateMessageCenterErrorIntent(activity.getApplicationContext());
		activity.startActivity(intent);
	}

	public static boolean canShowMessageCenterInternal(Context context) {
		return EngagementModule.canShowInteraction(context, "com.apptentive", "app", MessageCenterInteraction.DEFAULT_INTERNAL_EVENT_NAME);
	}

	public static Map<String, String> getAndClearCustomData() {
		Map<String, String> customData = ApptentiveInternal.customData;
		ApptentiveInternal.customData = null;
		return customData;
	}
}
