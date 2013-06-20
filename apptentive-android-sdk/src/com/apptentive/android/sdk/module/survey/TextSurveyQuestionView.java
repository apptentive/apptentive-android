/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.SurveyModule;
import com.apptentive.android.sdk.util.Constants;

import java.util.ArrayList;
import java.util.Set;


/**
 * @author Sky Kelsey.
 */
public class TextSurveyQuestionView extends SurveyItemView<BaseQuestion> {


	protected EditText answerText;

	public TextSurveyQuestionView(Context context, BaseQuestion question) {
		super(context, question);
	}

	@Override
	protected void initView() {
		super.initView();
		instructionsTextView.setText(question.isRequired() ? appContext.getString(R.string.apptentive_survey_required) : appContext.getString(R.string.apptentive_survey_optional));

		addSeparator();
		answerText = new EditText(appContext);
		answerText.setLayoutParams(Constants.ROW_LAYOUT);
		answerText.setBackgroundDrawable(null); // No crappy looking border.
		Set<String> answers = SurveyModule.getInstance().getSurveyState().getAnswers(question.getId());
		if(answers.size() > 0) {
			answerText.setText(new ArrayList<String>(answers).get(0));
		}
		answerText.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			public void afterTextChanged(Editable editable) {
				String questionId = question.getId();
				SurveyState state = SurveyModule.getInstance().getSurveyState();
				state.clearAnswers(questionId);
				state.addAnswer(questionId, editable.toString());
				updateInstructionsColor();
				fireListener();
			}
		});
		questionView.addView(answerText);
	}
}
