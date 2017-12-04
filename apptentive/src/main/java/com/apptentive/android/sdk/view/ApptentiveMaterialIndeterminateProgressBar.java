/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.apptentive.android.sdk.R;

/**
 * @author Sky Kelsey
 */
public class ApptentiveMaterialIndeterminateProgressBar extends ApptentiveMaterialDeterminateProgressBar {

	public ApptentiveMaterialIndeterminateProgressBar(Context context) {
		super(context);
	}

	public ApptentiveMaterialIndeterminateProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ApptentiveMaterialIndeterminateProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void start() {
		post(new Runnable() { // TODO: replace with DispatchQueue

			@Override
			public void run() {
				setProgress(50);
				Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.apptentive_material_inndeterminate_progress_bar);
				bar.startAnimation(animation);
			}
		});
	}

	public void stop() {
		post(new Runnable() { // TODO: replace with DispatchQueue

			@Override
			public void run() {
				setProgress(0);
				bar.clearAnimation();
			}
		});
	}
}
