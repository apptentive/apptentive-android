/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.apptentive.android.sdk.R;

/**
 * @author Sky Kelsey
 */
public class ApptentiveDialogButton extends FrameLayout {
	public ApptentiveDialogButton(Context context) {
		super(context);
		init(context);
	}

	public ApptentiveDialogButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ApptentiveDialogButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(context);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ApptentiveDialogButton, defStyle, 0);
		try {
			String label = a.getString(R.styleable.ApptentiveDialogButton_text);
			setText(label);
		} finally {
			a.recycle();
		}
	}

	private void init(Context context) {
		ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		setLayoutParams(layoutParams);
		inflate(context, R.layout.apptentive_dialog_button, this);
		setClickable(true);
	}

	public void setText(String label) {
		if (label != null) {
			((TextView) findViewById(R.id.label)).setText(label);
		}
	}

	OnClickListener onClickListener;

	@Override
	public void setOnClickListener(OnClickListener onClickListener) {
		super.setOnClickListener(onClickListener);
		this.onClickListener = onClickListener;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// This is a hack to get FrameLayout's OnClickListener to be fired correctly.
		return onClickListener != null;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		findViewById(R.id.background).setEnabled(enabled);
		findViewById(R.id.label).setEnabled(enabled);
	}
}
