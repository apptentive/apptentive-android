/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model.survey;

import com.apptentive.android.sdk.ApptentiveLog;

import static com.apptentive.android.sdk.ApptentiveLogTag.INTERACTIONS;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public interface Question {
	int QUESTION_TYPE_SINGLELINE  = 1;
	int QUESTION_TYPE_MULTICHOICE = 2;
	int QUESTION_TYPE_MULTISELECT = 3;
	int QUESTION_TYPE_RANGE = 4;

	int getType();

	String getId();
	String getValue();
	boolean isRequired();
	String getRequiredText();
	void setRequiredText(String requiredText);
	String getInstructions();
	String getErrorMessage();

	int getMinSelections();
	int getMaxSelections();

	enum Type {
		multichoice,
		singleline,
		multiselect,
		range,
		unknown;

		public static Type parse(String type) {
			try {
				return Type.valueOf(type);
			} catch (IllegalArgumentException e) {
				ApptentiveLog.v(INTERACTIONS, "Error parsing unknown Question.Type: " + type);
				logException(e);
			}
			return unknown;
		}
	}
}
