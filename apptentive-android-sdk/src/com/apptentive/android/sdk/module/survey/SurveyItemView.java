/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.SurveyModule;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey.
 */
abstract public class SurveyItemView<Q extends Question> extends FrameLayout {

	protected Context appContext;
	protected Q question;
	protected LinearLayout container;

	protected LinearLayout questionView;
	protected TextView titleTextView;
	protected TextView instructionsTextView;

	protected OnSurveyQuestionAnsweredListener listener;

	protected SurveyItemView(Context context, Q question) {
		super(context);
		this.appContext = context.getApplicationContext();
		this.question = question;
		initView();
		if (question != null) {
			setTitleText(question.getValue());
			instructionsTextView.setVisibility(View.VISIBLE);
			if (question.getInstructions() != null) {
				setInstructionsText(question.getInstructions());
			}
		}
	}

	protected void initView() {
		setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if(getContext() instanceof Activity) {
					Util.hideSoftKeyboard((Activity)getContext(), SurveyItemView.this);
				}
				return false;
			}
		});
		int dips12 = Util.dipsToPixels(appContext, 12);
		int dips16 = Util.dipsToPixels(appContext, 16);
		setPadding(0, dips16, 0, 0);
		container = new LinearLayout(appContext);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setBackgroundResource(R.drawable.apptentive_question_item);
		addView(container);

		titleTextView = new TextView(appContext);
		titleTextView.setLayoutParams(Constants.ROW_LAYOUT);
		titleTextView.setPadding(dips12, dips12, dips12, dips12);
		titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
		Resources resources = getContext().getResources();
		titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.apptentive_text_medium));
		titleTextView.setTextColor(resources.getColor(R.color.apptentive_survey_title_text));
		container.addView(titleTextView);

		instructionsTextView = new TextView(appContext);
		instructionsTextView.setLayoutParams(Constants.ROW_LAYOUT);
		instructionsTextView.setPadding(0, 0, dips16, 0);
		instructionsTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.apptentive_text_tiny));
		instructionsTextView.setVisibility(View.GONE);
		instructionsTextView.setGravity(Gravity.RIGHT);
		container.addView(instructionsTextView);

		questionView = new LinearLayout(appContext);
		questionView.setOrientation(LinearLayout.VERTICAL);
		container.addView(questionView);

		updateInstructionsColor();
	}

	public void setTitleText(String titleText) {
		this.titleTextView.setText(titleText);
	}

	public void setInstructionsText(String instructionsText) {
		this.instructionsTextView.setText(instructionsText);
	}

	protected void addSeparator() {
		LinearLayout separator = new LinearLayout(appContext);
		separator.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 1));
		separator.setBackgroundColor(R.color.apptentive_survey_question_answer_separator);
		questionView.addView(separator);
	}

	public void setOnSurveyQuestionAnsweredListener(OnSurveyQuestionAnsweredListener listener) {
		this.listener = listener;
	}

	protected void fireListener() {
		if (listener != null) {
			listener.onAnswered(this);
		}
	}

	protected void updateInstructionsColor() {
		if(question != null && question.isRequired() && !SurveyModule.getInstance().getSurveyState().isAnswered(question.getId())) {
			instructionsTextView.setTextColor(getContext().getResources().getColor(R.color.apptentive_survey_dialog_question_instruction_text_invalid));
		} else {
			instructionsTextView.setTextColor(getContext().getResources().getColor(R.color.apptentive_survey_dialog_question_instruction_text_valid));
		}
	}

	private boolean flashing = false;
	protected synchronized void flashInstructionsRed() {
		if(flashing) {
			return;
		}
		setClickable(false);
		instructionsTextView.setTextColor(getContext().getResources().getColor(R.color.apptentive_survey_dialog_question_instruction_text_invalid));
		flashing = true;
		instructionsTextView.post(new Runnable() {
			public void run() {
				try{
					Thread.sleep(300);
				}catch(InterruptedException e) {
				}
				instructionsTextView.setTextColor(getContext().getResources().getColor(R.color.apptentive_survey_dialog_question_instruction_text_valid));

				// A hack to make any pending clicks on this event go away.
				instructionsTextView.post(new Runnable() {
					public void run() {
						flashing = false;
						setClickable(true);
					}
				});
			}
		});
	}
}
