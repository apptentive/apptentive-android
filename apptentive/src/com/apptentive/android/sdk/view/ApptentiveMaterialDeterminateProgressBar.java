/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.apptentive.android.sdk.R;

/**
 * @author Sky Kelsey
 */
public class ApptentiveMaterialDeterminateProgressBar extends FrameLayout {

	protected static final int MIN = 0;
	protected static final int MAX = 100;

	int backgroundColor;
	int progressBarColor;

	int pendindProgress = -1;
	int progress = 0;
	View bar;
	View background;

	public ApptentiveMaterialDeterminateProgressBar(Context context) {
		super(context);
	}

	public ApptentiveMaterialDeterminateProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ApptentiveMaterialDeterminateProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ApptentiveMaterialDeterminateProgressBar, defStyleAttr, 0);
		try {
			progressBarColor = attributes.getColor(R.styleable.ApptentiveMaterialDeterminateProgressBar_apptentive_progressBarColor, Color.BLUE);
			backgroundColor = attributes.getColor(R.styleable.ApptentiveMaterialDeterminateProgressBar_apptentive_backgroundColor, desaturate(progressBarColor, 0.5f));
			progress = attributes.getInt(R.styleable.ApptentiveMaterialDeterminateProgressBar_apptentive_progress, 50);
		} finally {
			attributes.recycle();
		}
		setup();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (pendindProgress != -1) {
			setProgress(pendindProgress);
		}
	}

	private void setup() {

		background = new FrameLayout(getContext());
		background.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		background.setBackgroundColor(backgroundColor);
		addView(background);

		bar = new View(getContext());
		bar.setLayoutParams(new LayoutParams(1, 1));
		bar.setBackgroundColor(progressBarColor);
		addView(bar);
		setProgress(progress);
	}

	public void setProgress(int progress) {
		if (getWidth() == 0) {
			pendindProgress = progress;
		} else {
			this.progress = progress;
			progress = Math.min(progress, MAX);
			progress = Math.max(progress, MIN);
			int totalWidth = MAX - MIN;
			double progressRatio = (double) progress / (double) totalWidth;
			int progressWidth = (int) (getWidth() * progressRatio);
			LayoutParams params = (LayoutParams) bar.getLayoutParams();
			params.width = progressWidth;
			params.height = LayoutParams.MATCH_PARENT;
			bar.setLayoutParams(params);
			pendindProgress = -1;
		}
	}

	public int getProgress() {
		return progress;
	}

	protected int desaturate(int input, float desaturateRatio) {
		float[] hsv = new float[3];
		Color.colorToHSV(input, hsv);
		hsv[1] *= desaturateRatio;
		return Color.HSVToColor(hsv);
	}
}
