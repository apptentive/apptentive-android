/*
 * Constants.java
 *
 * Created by Sky Kelsey on 2011-10-09.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.util;

import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

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
}
