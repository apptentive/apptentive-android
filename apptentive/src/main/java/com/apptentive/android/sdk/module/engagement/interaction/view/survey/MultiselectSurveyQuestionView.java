/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.AnswerDefinition;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.MultiselectQuestion;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.SurveyState;
import com.apptentive.android.sdk.util.Util;

import java.util.List;

public class MultiselectSurveyQuestionView extends BaseSurveyQuestionView<MultiselectQuestion> implements CompoundButton.OnCheckedChangeListener {

	public MultiselectSurveyQuestionView(Context context, SurveyState surveyState, MultiselectQuestion question) {
		super(context, surveyState, question);

		View questionView = inflater.inflate(R.layout.apptentive_survey_question_multiselect, getAnswerContainer());

		List<AnswerDefinition> answerDefinitions = question.getAnswerChoices();
		LinearLayout choiceContainer = (LinearLayout) questionView.findViewById(R.id.choice_container);

		for (int i = 0; i < answerDefinitions.size(); i++) {
			AnswerDefinition answerDefinition = answerDefinitions.get(i);
			CheckBox choice = new CheckBox(contextThemeWrapper);
			choice.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) Util.dipsToPixels(contextThemeWrapper, 40)));
			choice.setText(answerDefinition.getValue());
			choice.setTag(R.id.apptentive_survey_answer_id, answerDefinition.getId());
			choice.setOnCheckedChangeListener(this);
			choiceContainer.addView(choice);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (getContext() instanceof Activity) {
			Util.hideSoftKeyboard(getContext(), MultiselectSurveyQuestionView.this);
		}
		fireListener();
	}
}
