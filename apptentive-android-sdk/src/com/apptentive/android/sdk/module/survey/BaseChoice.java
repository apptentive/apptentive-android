/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey.
 */
public abstract class BaseChoice extends FrameLayout {

	protected Context appContext;
	protected LinearLayout container;
	protected TextView textView;

	public BaseChoice(Context context) {
		super(context);
		appContext = context.getApplicationContext();
		initView();
	}

	protected void initView() {
		setLayoutParams(Constants.ROW_LAYOUT);
		setClickable(true);

		int pad4  = Util.dipsToPixels(appContext, 4);
		int pad12  = Util.dipsToPixels(appContext, 12);

		container = new LinearLayout(appContext);
		container.setLayoutParams(Constants.ROW_LAYOUT);
		container.setPadding(pad12, 0, pad4, 0);
		container.setGravity(Gravity.CENTER_VERTICAL);
		addView(container);

		textView = new TextView(appContext);
		textView.setTextColor(Color.BLACK);
		textView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1f));
		textView.setPadding(0, pad12, 0, pad12);
		container.addView(textView);
	}

	public void setText(String text) {
		textView.setText(text);
	}
}
