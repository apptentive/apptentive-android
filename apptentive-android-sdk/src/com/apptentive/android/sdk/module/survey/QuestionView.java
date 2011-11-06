/*
 * SurveyQuestionView.java
 *
 * Created by Sky Kelsey on 2011-10-09.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.widget.*;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;


public class QuestionView extends TableRow {

	private int index;
	private QuestionDefinition question;

	public QuestionView(Context context, QuestionDefinition question, int index) {
		super(context);
		this.question = question;
		this.index = index;
		render();
	}

	private void render() {
		setBackgroundColor(R.drawable.apptentive_question_item);

		int threeDips = Util.dipsToPixels(getContext(), 3);
		int tenDips = Util.dipsToPixels(getContext(), 10);

		TableLayout questionLayout = new TableLayout(this.getContext());
		questionLayout.setLayoutParams(Constants.rowLayout);
		questionLayout.setPadding(threeDips, threeDips, threeDips, threeDips);
		addView(questionLayout);

		TableRow questionRow = new TableRow(this.getContext());
		questionRow.setLayoutParams(Constants.rowLayout);
		questionRow.setPadding(0, 0, 0, tenDips);
		questionLayout.addView(questionRow);

		TableRow answerRow = new TableRow(this.getContext());
		answerRow.setLayoutParams(Constants.rowLayout);
		questionLayout.addView(answerRow);

		TextView indexText = new TextView(getContext());
		indexText.setText(this.index+")");
		questionRow.addView(indexText);

		TextView nameText = new TextView(getContext());
		nameText.setText("Name");
		questionRow.addView(nameText);

		TextView fillerView = new TextView(getContext());
		answerRow.addView(fillerView);

		TextView answerText = new TextView(getContext());
		answerText.setText("Answer");
		answerRow.addView(answerText);

	}

}
