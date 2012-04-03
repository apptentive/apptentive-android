package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;

/**
 * @author Sky Kelsey.
 */
public class SurveySendView extends SurveyItemView {
	public SurveySendView(Context context) {
		super(context);
		titleTextView.setTextColor(Color.BLUE);
		titleTextView.setGravity(Gravity.CENTER);
		titleTextView.setText("Send Response");
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
