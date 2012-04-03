/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.graphics.Color;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey.
 */
public class BaseChoice extends FrameLayout {

	protected Context appContext;
	protected LinearLayout container;
	protected TextView textView;

	public BaseChoice(Context context) {
		super(context);
		appContext = context;
		initView();
	}

	protected void initView() {
		setLayoutParams(Constants.ROW_LAYOUT);
		int pad5  = Util.dipsToPixels(appContext, 5);
		int pad10 = Util.dipsToPixels(appContext, 10);
		setPadding(pad10, pad5, pad5, pad5);
		setClickable(true);

		container = new LinearLayout(appContext);
		container.setLayoutParams(Constants.ROW_LAYOUT);
		addView(container);

		textView = new TextView(appContext);
		textView.setTextColor(Color.BLACK);
		textView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1f));
		container.addView(textView);
	}

	public void setText(String text) {
		textView.setText(text);
	}
}
