package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import com.apptentive.android.sdk.Log;

import java.util.List;

/**
 * @author Sky Kelsey.
 */
public class StackrankSurveyQuestionView extends SurveyItemView {

	// The order of the choice views IS the answer, so don't need a structure to hold answers.

	public StackrankSurveyQuestionView(Context context) {
		super(context);
	}

	public void setAnswers(List<AnswerDefinition> answerDefinitions) {
		for (AnswerDefinition answerDefinition : answerDefinitions) {
			addSeparator();

			final ReorderableChoice choice = new ReorderableChoice(appContext);
			choice.setText(answerDefinition.getValue());
			choice.setOnTouchListener(dragger);
			questionView.addView(choice);
		}
	}

	protected OnTouchListener dragger = new OnTouchListener() {
		public boolean onTouch(View view, MotionEvent motionEvent) {

			view.getParent().requestDisallowInterceptTouchEvent(true);

			int action = motionEvent.getAction();
			int x = (int)motionEvent.getRawX();
			int y = (int)motionEvent.getRawY();

			// TODO: Finish draging implementation.
			switch(action) {
				case MotionEvent.ACTION_DOWN:
					Log.e(String.format("onTouch(ACTION_DOWN, %04d, %04d)", x, y));
					break;
				case MotionEvent.ACTION_MOVE:
					Log.e(String.format("onTouch(ACTION_MOVE, %04d, %04d)", x, y));
					break;
				case MotionEvent.ACTION_UP:
					Log.e(String.format("onTouch(ACTION_UP,   %04d, %04d)", x, y));
					break;
			}
			return false;
		}
	};
}
