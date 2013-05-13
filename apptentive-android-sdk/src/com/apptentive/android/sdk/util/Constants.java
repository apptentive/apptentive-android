/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

/**
 * @author Sky Kelsey
 */
public class Constants {

	// Don't ever use "1.0". I prematurely incremented to 1.0, so we should skip over it.
	public static final String APPTENTIVE_SDK_VERSION = "1.0.1";


	public static final int REQUEST_CODE_PHOTO_FROM_MESSAGE_CENTER = 1000;

	public static final String PREF_NAME = "APPTENTIVE";

	public static final String PREF_KEY_CONVERSATION_TOKEN = "conversationToken";
	public static final String PREF_KEY_PERSON_ID = "personId";

	public static final String PREF_KEY_DEVICE = "device";
	public static final String PREF_KEY_SDK = "sdk";
	public static final String PREF_KEY_APP_RELEASE = "app_release";

	public static final String PREF_KEY_USER_ENTERED_EMAIL = "userEnteredEmail";

	public static final String PREF_KEY_APP_ACTIVITY_STATE_QUEUE = "appActivityStateQueue";

	public static final String PREF_KEY_APP_VERSION_CODE = "app_version_code";
	public static final String PREF_KEY_APP_MAIN_ACTIVITY_NAME = "mainActivityName";
	public static final String PREF_KEY_START_OF_RATING_PERIOD = "startOfRatingPeriod";
	public static final String PREF_KEY_RATING_STATE = "ratingState";
	public static final String PREF_KEY_RATING_EVENTS = "events";
	public static final String PREF_KEY_RATING_USES = "uses";

	public static final String PREF_KEY_AUTO_MESSAGE_SHOWN_NO_LOVE = "autoMessageShownNoLove";
	public static final String PREF_KEY_AUTO_MESSAGE_SHOWN_MANUAL = "autoMessageShownManual";

	public static final String PREF_KEY_APP_CONFIG_PREFIX = "appConfiguration.";
	public static final String PREF_KEY_APP_CONFIG_EXPIRATION = PREF_KEY_APP_CONFIG_PREFIX+"cache-expiration";
	public static final String PREF_KEY_APP_CONFIG_JSON = PREF_KEY_APP_CONFIG_PREFIX+"json";

	public static final int CONFIG_DEFAULT_APP_CONFIG_EXPIRATION_MILLIS = 0;
	public static final int CONFIG_DEFAULT_APP_CONFIG_EXPIRATION_DURATION_SECONDS = 86400; // 24 hours
	public static final int CONFIG_DEFAULT_DAYS_BEFORE_PROMPT = 30;
	public static final int CONFIG_DEFAULT_USES_BEFORE_PROMPT = 5;
	public static final int CONFIG_DEFAULT_SIGNIFICANT_EVENTS_BEFORE_PROMPT = 10;
	public static final int CONFIG_DEFAULT_DAYS_BEFORE_REPROMPTING = 5;
	public static final String CONFIG_DEFAULT_RATING_PROMPT_LOGIC = "{\"and\": [\"uses\",\"days\",\"events\"]}";
	public static final int CONFIG_DEFAULT_MESSAGE_CENTER_FG_POLL_SECONDS = 15;

	public static final String MANIFEST_KEY_APPTENTIVE_API_KEY = "apptentive_api_key";

	public static final ViewGroup.LayoutParams ROW_LAYOUT  = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	public static final ViewGroup.LayoutParams ITEM_LAYOUT = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

	private static final int SCREEN_ANIMATION_DURATION = 300;
	private static final Interpolator SCREEN_ANIMATION_INTERPOLATOR = new LinearInterpolator();

	public static Animation inFromRightAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

	public static Animation outToLeftAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

	public static Animation inFromLeftAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

	public static Animation outToRightAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

	public static Animation inFromBottomAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

	public static Animation outToTopAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

	public static Animation inFromTopAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

	public static Animation outToBottomAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

	/**
	 * A list of mobile carrier network types as Strings.
	 * From {@link android.telephony.TelephonyManager TelephonyManager}
	 * @see android.telephony.TelephonyManager
	 */
	private static final String[] networkTypeLookup = {
			"UNKNOWN", //  0
			"GPRS",    //  1
			"EDGE",    //  2
			"UMTS",    //  3
			"CDMA",    //  4
			"EVDO_0",  //  5
			"EVDO_A",  //  6
			"1xRTT",   //  7
			"HSDPA",   //  8
			"HSUPA",   //  9
			"HSPA",    // 10
			"IDEN",    // 11
			"EVDO_B",  // 12
			"LTE",     // 13
			"EHRPD",   // 14
			"HSPAP"    // 15
	};

	public static String networkTypeAsString(int networkTypeAsInt) {
		try {
			return networkTypeLookup[networkTypeAsInt];
		} catch (ArrayIndexOutOfBoundsException e) {
			return networkTypeLookup[0];
		}
	}

}
