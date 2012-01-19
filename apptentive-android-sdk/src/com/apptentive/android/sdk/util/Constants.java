/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
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
	public static final ViewGroup.LayoutParams rowLayout = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

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

	private static final String[] networkTypeLookup = {
			"NETWORK_TYPE_UNKNOWN", // 0
			"NETWORK_TYPE_GPRS",    //  1
			"NETWORK_TYPE_EDGE",    //  2
			"NETWORK_TYPE_UMTS",    //  3
			"NETWORK_TYPE_CDMA",    //  4
			"NETWORK_TYPE_EVDO_0",  //  5
			"NETWORK_TYPE_EVDO_A",  //  6
			"NETWORK_TYPE_1xRTT",   //  7
			"NETWORK_TYPE_HSDPA",   //  8
			"NETWORK_TYPE_HSUPA",   //  9
			"NETWORK_TYPE_HSPA",    // 10
			"NETWORK_TYPE_IDEN",    // 11
			"NETWORK_TYPE_EVDO_B",  // 12
			"NETWORK_TYPE_LTE",     // 13
			"NETWORK_TYPE_EHRPD",   // 14
			"NETWORK_TYPE_HSPAP"    // 15
	};

	public static String networkTypeAsString(int networkTypeAsInt) {
		try {
			return networkTypeLookup[networkTypeAsInt];
		} catch (ArrayIndexOutOfBoundsException e) {
			return networkTypeLookup[0];
		}
	}

}
