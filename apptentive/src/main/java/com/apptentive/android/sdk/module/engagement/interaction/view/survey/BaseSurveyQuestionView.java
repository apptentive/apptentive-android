/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.survey;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.fragment.app.Fragment;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.survey.OnSurveyQuestionAnsweredListener;
import com.apptentive.android.sdk.module.engagement.interaction.model.survey.Question;
import com.apptentive.android.sdk.util.StringUtils;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;


abstract public class BaseSurveyQuestionView<Q extends Question> extends Fragment implements SurveyQuestionView {

	private static final String SENT_METRIC = "sent_metric";

	protected Q question;

	private FrameLayout root;
	private TextView requiredView;
	private View dashView;
	private TextView instructionsView;
	private View validationFailedBorder;
    private TextView questionView;

	private boolean sentMetric;
	private OnSurveyQuestionAnsweredListener listener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.apptentive_survey_question_base, container, false);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		try {
			root = view.findViewById(R.id.question_base);
			requiredView = view.findViewById(R.id.question_required);
			dashView = view.findViewById(R.id.dash_view);
			instructionsView = view.findViewById(R.id.question_instructions);
            questionView = view.findViewById(R.id.question_title);

			// Makes UI tests easier. We can potentially obviate this if surveys used a RecyclerView.
			root.setTag(Integer.parseInt(getTag()));

            setQuestion(question.getValue());

			setInstructions(question.getRequiredText(), question.getInstructions());

			validationFailedBorder = view.findViewById(R.id.validation_failed_border);

			sentMetric = (savedInstanceState != null) && savedInstanceState.getBoolean(SENT_METRIC, false);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception in %s.onCreateView()", BaseSurveyQuestionView.class.getSimpleName());
			logException(e);
		}
	}

    private void setQuestion(String questionText) {
        questionView.setText(questionText);
        setQuestionAsHeadingForAccessibility();
    }

    private void setQuestionAsHeadingForAccessibility() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ViewCompat.setAccessibilityDelegate(questionView, new AccessibilityDelegateCompat() {
                @Override
                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    info.setHeading(true); // false to mark a view as not a heading
                }
            });
        }
    }


	private void setInstructions(String requiredText, String instructionsText) {
		boolean showRequiredText = question.isRequired();
		boolean showInstructions = !TextUtils.isEmpty(instructionsText);

		if (showRequiredText) {
			if (TextUtils.isEmpty(requiredText)) {
				requiredText = "Required";
			}
			requiredView.setText(requiredText);
			requiredView.setVisibility(View.VISIBLE);
		} else {
			requiredView.setVisibility(View.GONE);
		}

		if (showInstructions) {
			instructionsView.setText(instructionsText);
			instructionsView.setVisibility(View.VISIBLE);
		} else {
			instructionsView.setVisibility(View.GONE);
		}

		if (showRequiredText && showInstructions) {
			dashView.setVisibility(View.VISIBLE);
		} else {
			dashView.setVisibility(View.GONE);
		}

        setInstructionAndRequiredViewsAccessibilityImportance();
	}

    private void setInstructionAndRequiredViewsAccessibilityImportance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            instructionsView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            dashView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            requiredView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }

	protected LinearLayout getAnswerContainer(View rootView) {
		return (LinearLayout) rootView.findViewById(R.id.answer_container);
	}

	/**
	 * Always call this when the answer value changes.
	 */
	protected void fireListener() {
		if (listener != null) {
			listener.onAnswered(this);
		}
	}

    public void updateValidationState(boolean isValid) {
		validationFailedBorder.setVisibility(isValid ? View.INVISIBLE : View.VISIBLE);
		questionView.setContentDescription(getTitleContentDescription(isValid));
    }

    private String getTitleContentDescription(boolean isValid) {
        StringBuilder sb = new StringBuilder();
        if (!isValid && !StringUtils.isNullOrBlank(question.getErrorMessage())) {
            sb.append(question.getErrorMessage()).append(' ');
        }

        if (!StringUtils.isNullOrBlank(question.getValue())) sb.append(question.getValue());

        if (question.isRequired() && !StringUtils.isNullOrBlank(question.getRequiredText())) {
            sb.append('.').append(' ').append(question.getRequiredText());
        }

        if (!StringUtils.isNullOrBlank(question.getInstructions()))
            sb.append('.').append(' ').append(question.getInstructions());

        return sb.toString();
    }

	@Override
	public void setOnSurveyQuestionAnsweredListener(OnSurveyQuestionAnsweredListener listener) {
		this.listener = listener;
	}

	@Override
	public String getQuestionId() {
		return question.getId();
	}

	public abstract boolean isValid();

	public abstract Object getAnswer();

	@Override
	public String getErrorMessage() {
		return question.getErrorMessage();
	}

	@Override
	public boolean didSendMetric() {
		return sentMetric;
	}

	@Override
	public void setSentMetric(boolean sent) {
		sentMetric = sent;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(SENT_METRIC, sentMetric);
	}

    @Override
    public void focusOnQuestionTitleView() {
        questionView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
    }
}
