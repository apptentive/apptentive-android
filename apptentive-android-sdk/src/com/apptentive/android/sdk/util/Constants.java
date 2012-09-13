/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
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

	public static final String PREF_NAME = "APPTENTIVE";
	public static final String PREF_KEY_USER_ENTERED_EMAIL = "userEnteredEmail";
	public static final String PREF_KEY_APP_IN_BACKGROUND = "appIsInBackground";
	public static final String PREF_KEY_APP_ACTIVE_SESSION = "appActiveSession";

	public static final String PREF_KEY_APP_ACTIVE_ACTIVITY_NAMES = "appActiveActivityNames";

	public static final String PREF_KEY_APP_VERSION_CODE = "app_version_code";
	public static final String PREF_KEY_APP_MAIN_ACTIVITY_NAME = "mainActivityName";
	public static final String PREF_KEY_START_OF_RATING_PERIOD = "startOfRatingPeriod";
	public static final String PREF_KEY_RATING_STATE = "ratingState";
	public static final String PREF_KEY_RATING_EVENTS = "events";
	public static final String PREF_KEY_RATING_USES = "uses";

	public static final String PREF_KEY_APP_CONFIG_PREFIX = "appConfiguration.";
	public static final String PREF_KEY_APP_METRICS_ENABLED = PREF_KEY_APP_CONFIG_PREFIX+"metrics_enabled";
	public static final String PREF_KEY_APP_RATINGS_ENABLED = PREF_KEY_APP_CONFIG_PREFIX+"ratings_enabled";
	public static final String PREF_KEY_APP_RATINGS_CLEAR_ON_UPGRADE = PREF_KEY_APP_CONFIG_PREFIX+"ratings_clear_on_upgrade";
	public static final String PREF_KEY_APP_RATINGS_PROMPT_LOGIC = PREF_KEY_APP_CONFIG_PREFIX+"ratings_prompt_logic";
	public static final String PREF_KEY_APP_RATINGS_DAYS_BEFORE_PROMPT = PREF_KEY_APP_CONFIG_PREFIX+"ratings_days_before_prompt";
	public static final String PREF_KEY_APP_RATINGS_DAYS_BETWEEN_PROMPTS = PREF_KEY_APP_CONFIG_PREFIX+"ratings_days_between_prompts";
	public static final String PREF_KEY_APP_RATINGS_EVENTS_BEFORE_PROMPT = PREF_KEY_APP_CONFIG_PREFIX+"ratings_events_before_prompt";
	public static final String PREF_KEY_APP_RATINGS_USES_BEFORE_PROMPT = PREF_KEY_APP_CONFIG_PREFIX+"ratings_uses_before_prompt";

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
