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
abstract public class SurveyItemView<Q extends Question> extends FrameLayout {

	protected LinearLayout questionView;
	protected TextView titleTextView;
	protected Context appContext;
	protected OnSurveyQuestionAnsweredListener listener;
	protected Q question;

	protected SurveyItemView(Context context, Q question) {
		super(context);
		this.appContext = context.getApplicationContext();
		this.question = question;
		initView();
		if(question != null) {
			setTitleText(question.getValue());
		}
	}

	protected void initView() {
		int tenDips = Util.dipsToPixels(appContext, 10);
		setPadding(0, tenDips, 0, 0);

		questionView = new LinearLayout(appContext);
		questionView.setBackgroundResource(R.drawable.apptentive_question_item);
		questionView.setOrientation(LinearLayout.VERTICAL);
		addView(questionView);

		titleTextView = new TextView(appContext);
		titleTextView.setLayoutParams(Constants.ROW_LAYOUT);
		titleTextView.setPadding(tenDips, tenDips, tenDips, tenDips);
		titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
		titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f);
		titleTextView.setTextColor(Color.BLACK);
		questionView.addView(titleTextView, 0);
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
