/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class AnimationUtil {

	public static final int ANIMATION_DURATION = 300;

	public static final float SCALE_MAX = 1.5f;
	public static final float SCALE_DEFAULT = 1.0f;
	public static final float SCALE_MIN = 0.0f;
	public static final float ALPHA_MIN = 0.0f;
	public static final float ALPHA_DEFAULT = 1.0f;

	public AnimationUtil() {
	}

	public static void scaleFadeIn(View view) {
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", SCALE_MIN, SCALE_DEFAULT);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", SCALE_MIN, SCALE_DEFAULT);
		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", ALPHA_MIN, ALPHA_DEFAULT);
		view.setAlpha(ALPHA_MIN);
		view.setVisibility(View.VISIBLE);

		AnimatorSet animation = new AnimatorSet();
		animation.playTogether(scaleX, scaleY, alpha);
		animation.setInterpolator(new AccelerateDecelerateInterpolator());
		animation.setDuration(ANIMATION_DURATION);
		animation.start();
	}

	private static void scaleFadeOut(final View view, final int endVisibility) {
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", SCALE_MIN);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", SCALE_MIN);
		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", ALPHA_MIN);

		AnimatorSet animation = new AnimatorSet();
		animation.playTogether(scaleX, scaleY, alpha);
		animation.setInterpolator(new AccelerateDecelerateInterpolator());
		animation.setDuration(ANIMATION_DURATION);
		animation.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {

			}

			@Override
			public void onAnimationEnd(Animator animator) {
				view.setVisibility(endVisibility);
			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});
		animation.start();
	}

	public static void scaleFadeOutGone(final View view) {
		scaleFadeOut(view, View.GONE);
	}

	public static void scaleFadeOutInvisible(final View view) {
		scaleFadeOut(view, View.INVISIBLE);
	}


	public static void fadeIn(final View view, final Animator.AnimatorListener al) {
		if (view.getAlpha() == 1 || view.getVisibility() == View.VISIBLE) {
			return;
		}

		view.setAlpha(ALPHA_MIN);
		view.setVisibility(View.VISIBLE);

		ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", ALPHA_DEFAULT);

		if (al != null) {
			alphaAnimator.addListener(al);
		}
		alphaAnimator.start();
	}

	private static void fadeOut(final View view, final int endVisibility) {
		if (view.getAlpha() == 0 || view.getVisibility() == endVisibility) {
			return;
		}

		ObjectAnimator animation = ObjectAnimator.ofFloat(view, "alpha", ALPHA_MIN);

		animation.setDuration(ANIMATION_DURATION);
		animation.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {

			}

			@Override
			public void onAnimationEnd(Animator animator) {
				view.setVisibility(endVisibility);
			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});
		animation.start();
	}

	public static void fadeOutGone(final View view) {
		fadeOut(view, View.GONE);
	}

	public static void fadeOutInvisible(final View view) {
		fadeOut(view, View.INVISIBLE);
	}

	public static AnimatorSet buildListViewRowRemoveAnimator(final View view,
																													 Animator.AnimatorListener al,
																													 ValueAnimator.AnimatorUpdateListener vl
	) {

		AnimatorSet animatorSet = new AnimatorSet();
		Animator animX = ObjectAnimator.ofFloat(view, "rotationX", 0, 90);
		Animator animAlpha = ObjectAnimator.ofFloat(view, "alpha", 1, 0);
		ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
		if (vl != null) {
			valueAnimator.addUpdateListener(vl);
		}

		animX.setDuration(ANIMATION_DURATION);
		animAlpha.setDuration(ANIMATION_DURATION);
		valueAnimator.setDuration(ANIMATION_DURATION + ANIMATION_DURATION + 100);
		animatorSet.playTogether(animX, animAlpha, valueAnimator);
		if (al != null) {
			animatorSet.addListener(al);
		}
		return animatorSet;
	}

	public static AnimatorSet buildListViewRowShowAnimator(final View view,
																												 Animator.AnimatorListener al,
																												 ValueAnimator.AnimatorUpdateListener vl
	) {

		AnimatorSet animatorSet = new AnimatorSet();
		Animator animX = ObjectAnimator.ofFloat(view, "rotationX", -90, 0);
		Animator animAlpha = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
		ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
		if (vl != null) {
			valueAnimator.addUpdateListener(vl);
		}

		animX.setDuration(ANIMATION_DURATION);
		animAlpha.setDuration(ANIMATION_DURATION);
		valueAnimator.setDuration(ANIMATION_DURATION);
		animatorSet.playTogether(animX, animAlpha, valueAnimator);
		if (al != null) {
			animatorSet.addListener(al);
		}
		return animatorSet;
	}
}