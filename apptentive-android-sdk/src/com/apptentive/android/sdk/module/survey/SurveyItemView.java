/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey.
 */
abstract public class SurveyItemView extends FrameLayout {

	protected LinearLayout questionView;
	protected TextView titleTextView;
	protected Context appContext;
	protected OnSurveyQuestionAnsweredListener listener;


	protected SurveyItemView(Context context) {
		super(context);
		this.appContext = context.getApplicationContext();
		initView();
	}

	protected void initView() {
		int dip10 = Util.dipsToPixels(appContext, 10);
		setPadding(0, dip10, 0, 0);

		LinearLayout innerLayout = new LinearLayout(appContext);
		innerLayout.setBackgroundResource(R.drawable.apptentive_question_item);
		innerLayout.setOrientation(LinearLayout.VERTICAL);
		addView(innerLayout);

		titleTextView = new TextView(appContext);
		titleTextView.setLayoutParams(Constants.ROW_LAYOUT);
		titleTextView.setPadding(dip10, dip10, dip10, dip10);
		titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
		titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f);
		titleTextView.setTextColor(Color.BLACK);
		innerLayout.addView(titleTextView);

		questionView = new LinearLayout(appContext);
		questionView.setOrientation(LinearLayout.VERTICAL);
		innerLayout.addView(questionView);
	}

	public void setTitleText(String titleText) {
		this.titleTextView.setText(titleText);
	}

	protected void addSeparator() {
		LinearLayout separator = new LinearLayout(appContext);
		separator.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 1));
		separator.setBackgroundColor(Color.LTGRAY);
		questionView.addView(separator);
	}

	public void setOnSurveyQuestionAnsweredListener(OnSurveyQuestionAnsweredListener listener) {
		this.listener = listener;
	}

	protected void fireListener() {
		if(listener != null) {
			listener.onAnswered(this);
		}
	}
}
