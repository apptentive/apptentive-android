/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import com.apptentive.android.sdk.util.Constants;

/**
 * @author Sky Kelsey.
 */
public class TextSurveyQuestionView extends SurveyItemView {


	protected EditText answerText;

	public TextSurveyQuestionView(Context context) {
		super(context);
	}

	@Override
	protected void initView() {
		super.initView();
		addSeparator();
		answerText = new EditText(appContext);
		answerText.setLayoutParams(Constants.ROW_LAYOUT);
		answerText.setBackgroundDrawable(null); // No crappy looking border.
		answerText.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			public void afterTextChanged(Editable editable) {
				fireListener();
			}
		});
		questionView.addView(answerText);
	}

	public String getAnswer() {
		return answerText.getText().toString();
	}

}
