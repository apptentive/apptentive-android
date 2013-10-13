/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import com.apptentive.android.sdk.R;

/**
 * @author Sky Kelsey.
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class SurveySendView extends SurveyItemView {

	public SurveySendView(Context context) {
		super(context, null);
		setTitleText(context.getString(R.string.apptentive_survey_send_response));
		titleTextView.setTextColor(Color.BLUE);
		titleTextView.setGravity(Gravity.CENTER);
	}

	public void setEnabled(boolean enabled) {
		if(enabled) {
			titleTextView.setTextColor(Color.BLUE);
			setClickable(true);
		} else {
			titleTextView.setTextColor(Color.GRAY);
			setClickable(false);
		}
	}

	@Override
	public void setClickable(boolean clickable) {
		super.setClickable(clickable);
	}
}
